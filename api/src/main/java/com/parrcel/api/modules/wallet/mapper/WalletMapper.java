package com.parrcel.api.modules.wallet.mapper;


import com.parrcel.api.modules.wallet.dto.WalletResponse;
import com.parrcel.api.modules.wallet.dto.WalletTransactionResponse;
import com.parrcel.api.modules.wallet.entity.Wallet;
import com.parrcel.api.modules.wallet.entity.WalletTransaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    WalletResponse toResponse(Wallet wallet);
    WalletTransactionResponse toWalletTransactionresponse(WalletTransaction walletTransaction);
}
