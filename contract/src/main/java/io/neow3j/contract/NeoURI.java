package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.utils.AddressUtils;
import io.neow3j.utils.Strings;
import io.neow3j.wallet.Wallet;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;

/**
 * Wrapper class to generate NEP-9 compatible URI schemes for NEP-5 Token transfers.
 */
public class NeoURI {

    private URI uri;

    private Neow3j neow3j;
    private Wallet wallet;
    private ScriptHash address;
    private String asset;
    private BigDecimal amount;

    private static final String NEO_SCHEME = "neo";
    private static final String TRANSFER_FUNCTION = "transfer";

    public NeoURI() {
    }

    public NeoURI(Neow3j neow3j) {
        this.neow3j = neow3j;
    }

    /**
     * Creates a NeoURI from a NEP-9 URI String.
     *
     * @param uriString A NEP-9 URI String.
     * @return A NeoURI object
     * @throws IllegalFormatException if the provided URI has an invalid format.
     */
    public static NeoURI fromURI(String uriString) throws IllegalFormatException {
        if (uriString == null) throw new IllegalArgumentException("The provided String is null.");
        String[] baseAndQuery = uriString.split("\\?");
        String[] beginTx = baseAndQuery[0].split(":");

        if (beginTx.length != 2 || !beginTx[0].equals(NEO_SCHEME)) {
            throw new IllegalArgumentException("Invalid uri.");
        }
        NeoURI neoURI = new NeoURI();

        // Add the address
        neoURI.toAddress(beginTx[1]);

        // Add the optional parts of the uri - asset and amount.
        if (baseAndQuery.length == 2) {
            String[] query = baseAndQuery[1].split("&");
            for (String singleQuery : query) {
                String[] singleQueryParts = singleQuery.split("=", 2);
                if (singleQueryParts.length != 2) throw new IllegalArgumentException("This uri contains invalid queries.");
                if (singleQueryParts[0].equals("asset") && neoURI.asset == null) {
                    neoURI.asset = singleQueryParts[1];
                } else if (singleQueryParts[0].equals("amount") && neoURI.amount == null) {
                    neoURI.amount = new BigDecimal(singleQueryParts[1]);
                }
            }
        }
        return neoURI;
    }

    /**
     * Builds a {@code TransactionBuilder} from this NeoURI.
     * <p>
     * Needs all necessary parameters to create a transfer invocation.
     *
     * @return a TransactionBuilder object.
     */
    public TransactionBuilder transferBuilder() throws IOException {
        if (neow3j == null) {
            throw new IllegalStateException("Neow3j instance is not set.");
        }
        if (address == null) {
            throw new IllegalStateException("Recipient not set.");
        }
        if (wallet == null) {
            throw new IllegalStateException("Wallet not set.");
        }
        if (amount == null) {
            throw new IllegalStateException("Amount is not set.");
        }

        BigInteger fractions;
        BigDecimal factor;
        ScriptHash contract;
        if (isNeoToken(asset)) {
            factor = BigDecimal.TEN.pow(NeoToken.DECIMALS);
            fractions = amount.multiply(factor).toBigInteger();
            contract = NeoToken.SCRIPT_HASH;
        } else if (isGasToken(asset)) {
            factor = BigDecimal.TEN.pow(GasToken.DECIMALS);
            fractions = amount.multiply(factor).toBigInteger();
            contract = GasToken.SCRIPT_HASH;
        } else {
            fractions = computeFractions(neow3j, asset);
            contract = new ScriptHash(asset);
        }

        return new SmartContract(contract, neow3j)
                .invokeFunction(TRANSFER_FUNCTION,
                        ContractParameter.hash160(wallet.getDefaultAccount().getScriptHash()),
                        ContractParameter.hash160(address),
                        ContractParameter.integer(fractions))
                .wallet(wallet);
    }

    private boolean isNeoToken(String asset) {
        return asset.equals(NeoToken.SCRIPT_HASH.toString()) || asset.equals("neo");
    }

    private boolean isGasToken(String asset) {
        return asset.equals(GasToken.SCRIPT_HASH.toString()) || asset.equals("gas");
    }

    private BigInteger computeFractions(Neow3j neow3j, String asset) throws IOException {
        int decimals = new Nep5Token(new ScriptHash(asset), neow3j).getDecimals();
        BigDecimal factor = BigDecimal.TEN.pow(decimals);
        return this.amount.multiply(factor).toBigInteger();
    }

    public NeoURI toAddress(String address) {
        if (!AddressUtils.isValidAddress(address)) {
            throw new IllegalArgumentException("Invalid address used.");
        }

        this.address = ScriptHash.fromAddress(address);
        return this;
    }

    public NeoURI toAddress(ScriptHash address) {
        this.address = address;
        return this;
    }

    // TODO: This method does the same as the second method below. What shoul done differently?
    public NeoURI assetFromByteArray(String asset) {
        this.asset = asset;
        return this;
    }

    public NeoURI asset(ScriptHash asset) {
        this.asset = asset.toString();
        return this;
    }

    public NeoURI asset(String asset) {
        this.asset = asset;
        return this;
    }

    public NeoURI amount(String amount) {
        this.amount = new BigDecimal(amount);
        return this;
    }

    public NeoURI amount(Integer amount) {
        this.amount = new BigDecimal(amount);
        return this;
    }

    public NeoURI amount(BigInteger amount) {
        this.amount = new BigDecimal(amount);
        return this;
    }

    public NeoURI amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public NeoURI neow3j(Neow3j neow3j) {
        this.neow3j = neow3j;
        return this;
    }

    public NeoURI wallet(Wallet wallet) {
        this.wallet = wallet;
        return this;
    }

    private String buildQueryPart() {
        List<String> query = new ArrayList<>();
        if (this.asset != null) {
            query.add("asset=" + this.asset);
        }
        if (this.amount != null) {
            query.add("amount=" + this.amount.toString());
        }
        return Strings.join(query, "&");
    }

    public NeoURI buildURI() {
        if (this.address == null) {
            throw new IllegalStateException("Recipient address not set.");
        }
        String basePart = NEO_SCHEME + ":" + this.address.toAddress();
        String queryPart = buildQueryPart();

        String uri;
        if (queryPart.equals("")) {
            uri = basePart;
        } else {
            uri = basePart + "?" + queryPart;
        }

        this.uri = URI.create(uri);
        return this;
    }

    // Getters

    public URI getURI() {
        return this.uri;
    }

    public String getURIAsString() {
        return this.uri.toString();
    }

    public String getAddress() {
        return this.address.toAddress();
    }

    public ScriptHash getAddressAsScriptHash() {
        return this.address;
    }

    public String getAsset() {
        return this.asset;
    }

    public ScriptHash getAssetAsScriptHash() {
        if (this.asset.equals("neo")) {
            return NeoToken.SCRIPT_HASH;
        } else if (this.asset.equals("gas")) {
            return GasToken.SCRIPT_HASH;
        } else {
            return new ScriptHash(this.asset);
        }
    }

    public String getAssetAsAddress() {
        if (this.asset.equals("neo")) {
            return NeoToken.SCRIPT_HASH.toAddress();
        } else if (this.asset.equals("gas")) {
            return GasToken.SCRIPT_HASH.toAddress();
        } else {
            return new ScriptHash(this.asset).toAddress();
        }
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public String getAmountAsString() {
        return this.amount.toString();
    }
}
