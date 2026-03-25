package com.parrcel.api.modules.wallet;

/**
 * Reference type constants used when creating wallet transactions.
 * These identify what business event a transaction is linked to.
 */
public final class WalletReferenceType {

    public static final String DELIVERY_FROM_AGENT_FEE = "DELIVERY_FROM_AGENT_FEE";
    public static final String DELIVERY_TO_AGENT_FEE = "DELIVERY_TO_AGENT_FEE";
    public static final String DELIVERY_COD = "DELIVERY_COD";

    private WalletReferenceType() {}
}
