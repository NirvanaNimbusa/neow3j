package io.neow3j.devpack.neo;

import io.neow3j.devpack.annotations.Contract;

@Contract(scriptHash = "0x668e0c1f9d7b70a99dd9e06eadd4c784d641afbc")
public class GAS {

    /**
     * Gets the name of the GAS token contract.
     *
     * @return the name.
     */
    public static native String name();

    /**
     * Gets the symbol of the GAS token.
     *
     * @return the symbol.
     */
    public static native String symbol();

    /**
     * Gets the number of decimals of the GAS token.
     *
     * @return the number of decimals.
     */
    public static native int decimals();

    /**
     * Gets the total supply of the GAS token.
     *
     * @return the total supply.
     */
    public static native int totalSupply();

    /**
     * Gets the GAS balance of the given account.
     *
     * @param scriptHash The script hash of the account to get the balance for.
     * @return the account's balance.
     */
    public static native int balanceOf(byte[] scriptHash);

}
