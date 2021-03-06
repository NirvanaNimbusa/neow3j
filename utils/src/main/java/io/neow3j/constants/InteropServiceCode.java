package io.neow3j.constants;

import io.neow3j.crypto.Hash;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import java.nio.charset.StandardCharsets;

public enum InteropServiceCode {

    SYSTEM_ENUMERATOR_CREATE("System.Enumerator.Create", 400),
    SYSTEM_ENUMERATOR_NEXT("System.Enumerator.Next", 1_000_000),
    SYSTEM_ENUMERATOR_VALUE("System.Enumerator.Value", 400),
    SYSTEM_ENUMERATOR_CONCAT("System.Enumerator.Concat", 400),

    SYSTEM_ITERATOR_CREATE("System.Iterator.Create", 400),
    SYSTEM_ITERATOR_KEY("System.Iterator.Key", 400),
    SYSTEM_ITERATOR_KEYS("System.Iterator.Keys", 400),
    SYSTEM_ITERATOR_VALUES("System.Iterator.Values", 400),
    SYSTEM_ITERATOR_CONCAT("System.Iterator.Concat", 400),

    SYSTEM_RUNTIME_PLATFORM("System.Runtime.Platform", 250),
    SYSTEM_RUNTIME_GETTRIGGER("System.Runtime.GetTrigger", 250),
    SYSTEM_RUNTIME_GETTIME("System.Runtime.GetTime", 250),
    SYSTEM_RUNTIME_GETSCRIPTCONTAINER("System.Runtime.GetScriptContainer", 250),
    SYSTEM_RUNTIME_GETEXECUTINGSCRIPTHASH("System.Runtime.GetExecutingScriptHash", 400),
    SYSTEM_RUNTIME_GETCALLINGSCRIPTHASH("System.Runtime.GetCallingScriptHash", 400),
    SYSTEM_RUNTIME_GETENTRYSCRIPTHASH("System.Runtime.GetEntryScriptHash", 400),
    SYSTEM_RUNTIME_CHECKWITNESS("System.Runtime.CheckWitness", 30_000),
    SYSTEM_RUNTIME_GETINVOCATIONCOUNTER("System.Runtime.GetInvocationCounter", 400),
    SYSTEM_RUNTIME_LOG("System.Runtime.Log", 1_000_000),
    SYSTEM_RUNTIME_NOTIFY("System.Runtime.Notify", 1_000_000),
    SYSTEM_RUNTIME_GETNOTIFICATIONS("System.Runtime.GetNotifications", 10_000),
    SYSTEM_RUNTIME_GASLEFT("System.Runtime.GasLeft", 400),

    SYSTEM_STORAGE_GETCONTEXT("System.Storage.GetContext", 400),
    SYSTEM_STORAGE_GETREADONLYCONTEXT("System.Storage.GetReadOnlyContext", 400),
    SYSTEM_STORAGE_ASREADONLY("System.Storage.AsReadOnly", 400),
    SYSTEM_STORAGE_GET("System.Storage.Get", 1_000_000),
    SYSTEM_STORAGE_FIND("System.Storage.Find", 1_000_000),
    SYSTEM_STORAGE_PUT("System.Storage.Put", null), // dynamic calculation
    SYSTEM_STORAGE_PUTEX("System.Storage.PutEx", null), // dynamic calculation
    SYSTEM_STORAGE_DELETE("System.Storage.Delete", 100_000),

    SYSTEM_BINARY_SERIALIZE("System.Binary.Serialize", 100_000),
    SYSTEM_BINARY_DESERIALIZE("System.Binary.Deserialize", 500_000),
    SYSTEM_BINARY_BASE64ENCODE("System.Binary.Base64Encode", 100_000),
    SYSTEM_BINARY_BASE64DECODE("System.Binary.Base64Decode", 100_000),

    SYSTEM_BLOCKCHAIN_GETHEIGHT("System.Blockchain.GetHeight", 400),
    SYSTEM_BLOCKCHAIN_GETBLOCK("System.Blockchain.GetBlock", 2_500_000),
    SYSTEM_BLOCKCHAIN_GETTRANSACTION("System.Blockchain.GetTransaction", 1_000_000),
    SYSTEM_BLOCKCHAIN_GETTRANSACTIONHEIGHT("System.Blockchain.GetTransactionHeight", 1_000_000),
    SYSTEM_BLOCKCHAIN_GETTRANSACTIONFROMBLOCK("System.Blockchain.GetTransactionFromBlock",
            1_000_000),
    SYSTEM_BLOCKCHAIN_GETCONTRACT("System.Blockchain.GetContract", 1_000_000),

    SYSTEM_CALLBACK_CREATE("System.Callback.Create", 400), // DONE
    SYSTEM_CALLBACK_CREATEFROMMETHOD("System.Callback.CreateFromMethod", 1_000_000),
    SYSTEM_CALLBACK_CREATEFROMSYSCALL("System.Callback.CreateFromSyscall", 400),
    SYSTEM_CALLBACK_INVOKE("System.Callback.Invoke", 1_000_000),

    SYSTEM_CONTRACT_CREATE("System.Contract.Create", null), // dynamic calculation
    SYSTEM_CONTRACT_UPDATE("System.Contract.Update", null), // dynamic calculation
    SYSTEM_CONTRACT_DESTROY("System.Contract.Destroy", 1_000_000),
    SYSTEM_CONTRACT_CALL("System.Contract.Call", 1_000_000),
    SYSTEM_CONTRACT_CALLEX("System.Contract.CallEx", 1_000_000),
    SYSTEM_CONTRACT_ISSTANDARD("System.Contract.IsStandard", 30_000),
    SYSTEM_CONTRACT_GETCALLFLAGS("System.Contract.GetCallFlags", 30_000),
    SYSTEM_CONTRACT_CREATESTANDARDACCOUNT("System.Contract.CreateStandardAccount", 10_000),

    SYSTEM_JSON_SERIALIZE("System.Json.Serialize", 100_000),
    SYSTEM_JSON_DESERIALIZE("System.Json.Deserialize", 500_000),

    NEO_CRYPTO_SHA256("Neo.Crypto.SHA256", 1_000_000),
    NEO_CRYPTO_RIPEMD160("Neo.Crypto.RIPEMD160", 1_000_000),
    NEO_CRYPTO_VERIFYWITHECDSASECP256R1("Neo.Crypto.VerifyWithECDsaSecp256r1", 1_000_000),
    NEO_CRYPTO_VERIFYWITHECDSASECP256K1("Neo.Crypto.VerifyWithECDsaSecp256k1", 1_000_000),
    // The price for check multisig is the price for Secp256r1.Verify times the number of signatures
    NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256R1("Neo.Crypto.CheckMultisigWithECDsaSecp256r1", null),
    // The price for check multisig is the price for Secp256k1.Verify times the number of signatures
    NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256K1("Neo.Crypto.CheckMultisigWithECDsaSecp256k1", null),

    NEO_NATIVE_DEPLOY("Neo.Native.Deploy", null),
    NEO_NATIVE_CALL("Neo.Native.Call", null);

    /* The service's name */
    private String name;
    /* Price in fractions of GAS for executing the service. */
    private Long price;

    /**
     * Constructs a new interop service code.
     *
     * @param name  The name of the service.
     * @param price The execution GAS price of the code.
     */
    InteropServiceCode(String name, Integer price) {
        this.name = name;
        if (price != null) {
            this.price = (long) price;
        }
    }

    public String getName() {
        return this.name;
    }

    /**
     * Gets this {@code InteropServiceCode}'s hash (4 bytes) as a hex string.
     *
     * @return the hash.
     */
    public String getHash() {
        byte[] sha256 = Hash.sha256(this.getName().getBytes(StandardCharsets.US_ASCII));
        return Numeric.toHexStringNoPrefix(ArrayUtils.getFirstNBytes(sha256, 4));
    }

    /**
     * Get the price in fractions of GAS of this interop service.
     * <p>
     * For some interop service the price depends on an additional parameter. In that case use
     * {@link InteropServiceCode#getPrice(int)}.
     *
     * @return the price
     * @throws UnsupportedOperationException if this interop service has a dynamic price.
     */
    public long getPrice() {
        switch (this) {
            case SYSTEM_CONTRACT_CREATE:
                throw new UnsupportedOperationException("The price of the interop service "
                        + "System.Contract.Create is not fixed but depends on the contract's "
                        + "script and manifest size.");
            case NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256R1:
            case NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256K1:
                throw new UnsupportedOperationException("The price of the interop service "
                        + this.getName() + " is not fixed but depends on the number of "
                        + "signatures.");
            default:
                return this.price;
        }
    }

    /**
     * Get the price in fractions of GAS of this interop service dependent on the given parameter.
     * <p>
     * If the interop service has a fixed price, the parameter is simply ignored and the fixed price
     * is returned.
     *
     * @return the price
     */
    public long getPrice(int param) {
        if (this.price != null) {
            return this.price;
        }
        switch (this) {
            case SYSTEM_CONTRACT_CREATE:
                return param * NeoConstants.GAS_PER_BYTE;
            case NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256R1:
                return param * NEO_CRYPTO_VERIFYWITHECDSASECP256R1.price;
            case NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256K1:
                return param * NEO_CRYPTO_VERIFYWITHECDSASECP256K1.price;
            default:
                throw new UnsupportedOperationException("The price for " + this.toString() + " is "
                        + "not defined.");
        }
    }

    @Override
    public String toString() {
        return getName();
    }

}
