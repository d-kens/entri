package com.entri.wallet;

import com.entri.wallet.dto.WalletResponse;

public interface WalletProvider {
    String createWallet(String label);
    WalletResponse getWallet(String walletId);
}
