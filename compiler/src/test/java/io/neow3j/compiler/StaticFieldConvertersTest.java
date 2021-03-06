package io.neow3j.compiler;

import io.neow3j.devpack.Helper;
import io.neow3j.devpack.neo.Runtime;
import java.io.IOException;
import java.util.Arrays;
import org.hamcrest.core.StringContains;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StaticFieldConvertersTest {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void invalidAddressStringLiteralThrowsCompilerExceptions() throws IOException {
        expected.expect(CompilerException.class);
        expected.expectMessage(new StringContainsInOrder(
                Arrays.asList("Invalid address", "A0unErzotcQTNWP2qktA7LgkXZVdHea97H")));
        new Compiler().compileClass(InvalidAddressVariable.class.getName());
    }

    @Test
    public void invalidHexStringLiteralThrowsCompilerExceptions() throws IOException {
        expected.expect(CompilerException.class);
        expected.expectMessage(new StringContainsInOrder(
                Arrays.asList("Invalid hex string", "0x0h02")));
        new Compiler().compileClass(InvalidHexStringVariable.class.getName());
    }

    @Test
    public void invalidIntegerStringLiteralThrowsCompilerExceptions() throws IOException {
        expected.expect(CompilerException.class);
        expected.expectMessage(new StringContainsInOrder(
                Arrays.asList("Invalid number string", "100e0000000000000000000000000000")));
        new Compiler().compileClass(InvalidIntStringVariable.class.getName());
    }

    @Test
    public void illegalInputToConverterMethodLeadsToCompilerException() throws IOException {
        expected.expect(CompilerException.class);
        expected.expectMessage(new StringContains("constant string literals"));
        new Compiler().compileClass(IllegalInputConverterMethod.class.getName());
    }

    @Test
    public void illegalUseOfConverterMethodLeadsToCompilerException() throws IOException {
        expected.expect(CompilerException.class);
        expected.expectMessage(new StringContains("static variable initialization scope"));
        new Compiler().compileClass(IllegalUseOfConverterMethod.class.getName());
    }
}

class InvalidAddressVariable {

    private static final byte[] scriptHash = Helper.addressToScriptHash(
            "A0unErzotcQTNWP2qktA7LgkXZVdHea97H");

    public static byte[] main() {
        return scriptHash;
    }
}

class InvalidHexStringVariable {

    private static final byte[] bytes = Helper.hexToBytes("0x0h02");

    public static byte[] main() {
        return bytes;
    }
}

class InvalidIntStringVariable {

    private static final int integer = Helper.stringToInt("100e0000000000000000000000000000");

    public static int main() {
        return integer;
    }
}

class IllegalInputConverterMethod {

    private static final byte[] bytes = Helper.hexToBytes(Runtime.getPlatform());

    public static byte[] main() {
        return bytes;
    }

}

class IllegalUseOfConverterMethod {

    private static final byte[] scriptHash = getScriptHash();

    public static byte[] main() {
        return scriptHash;
    }

    public static byte[] getScriptHash() {
        return Helper.addressToScriptHash("AJunErzotcQTNWP2qktA7LgkXZVdHea97H");
    }
}
