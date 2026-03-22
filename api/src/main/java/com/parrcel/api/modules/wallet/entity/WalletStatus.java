package com.parrcel.api.modules.wallet.entity;

public enum WalletStatus {
    ACTIVE, // Can credit or debit
    SUSPENDED, // Temporarily blocked - can credit but cannot withdraw
    CLOSED // Permanently closed - no transactions allowed
}
