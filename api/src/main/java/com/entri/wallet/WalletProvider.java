package com.entri.wallet;

import com.entri.wallet.dto.WalletResponse;

public interface WalletProvider {
    String createWallet(String label, String currency);
    WalletResponse getWallet(String walletId);
}
