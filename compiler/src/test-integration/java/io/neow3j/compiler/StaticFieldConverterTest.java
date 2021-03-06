package io.neow3j.compiler;

import io.neow3j.devpack.Helper;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Ignore;

public class StaticFieldConverterTest extends CompilerTest {

    @BeforeClass
    public static void setUp() throws Exception {
        setUp(StaticFieldConverter.class.getName());
    }

    @Ignore("The return type of this function call is `Any` even though we would expect a "
            + "`ByteArray`. There is a question at https://github.com/neo-project/neo-vm/issues/368"
            + " which asks if this is intended behavior of the VM. Additionally the `Buffer` "
            + "`StackItemType` is not yet implemented in neow3j.")
    public void addressToScriptHashConverter() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(getTestName());
//            assertThat(response.getInvocationResult().getStack().get(0).asBuffer().getValue(),
//                    is(expected));
    }

    @Ignore("The return type of this function call is `Any` even though we would expect a "
            + "`ByteArray`. There is a question at https://github.com/neo-project/neo-vm/issues/368"
            + " which asks if this is intended behavior of the VM. Additionally the `Buffer` "
            + "`StackItemType` is not yet implemented in neow3j.")
    public void hexStringToByteArrayConverter() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(getTestName());
//            assertThat(response.getInvocationResult().getStack().get(0).asBuffer().getValue(),
//                    is(expected));
    }

    @Ignore("The return type of this function call is `Any` even though we would expect a "
            + "`Integer`. There is a question at https://github.com/neo-project/neo-vm/issues/368 "
            + "which asks if this is intended behavior of the VM.")

    public void stringToIntegerConverter() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(getTestName());
//            assertThat(response.getInvocationResult().getStack().get(0).asInteger().getValue(),
//                    is(expected));
    }
}

class StaticFieldConverter {

    private static final byte[] scriptHash = Helper.addressToScriptHash(
            "AJunErzotcQTNWP2qktA7LgkXZVdHea97H");
    private static final byte[] bytes = Helper.hexToBytes("0x010203");
    private static final int integer = Helper.stringToInt("1000000000000000000000000000000");

    public static byte[] addressToScriptHashConverter() {
        return scriptHash;
    }

    public static byte[] hexStringToByteArrayConverter() {
        return bytes;
    }

    public static int stringToIntegerConverter() {
        return integer;
    }
}
