package io.neow3j.devpack;

public class StaticVariableHelper {

    /**
     * Converts the given Neo address to its script hash (little-endian byte array).
     * <p>
     * This method can only be applied to constant string literals in static variable
     * initializations.
     * <p>
     * Example: addressToScriptHash("AFsCjUGzicZmXQtWpwVt6hNeJTBwSipJMS") generates
     * 0102030405060708090a0b0c0d0e0faabbccddee
     */
    public static native byte[] addressToScriptHash(String address);

    /**
     * Converts the given hex string to a byte array.
     * <p>
     * This method can only be applied to constant string literals in static variable
     * initializations.
     * <p>
     * Example: hexToBytes("0102030405060708090a0b0c0d0e0faabbccddee") generates the corresponding
     * byte array.
     */
    public static native byte[] hexToBytes(String hex);

    /**
     * Converts the given number string to an integer.
     * <p>
     * This method can only be applied to constant string literals in static variable
     * initializations.
     * <p>
     * Example: stringToInt("10000") generates the int with the value 10000.
     */
    public static native int stringToInt(String text);
}