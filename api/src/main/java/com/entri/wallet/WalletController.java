package com.entri.wallet;

import com.entri.common.dto.PaginationResponse;
import com.entri.security.UserPrincipal;
import com.entri.wallet.dto.WalletResponse;
import com.entri.wallet.dto.WalletTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WalletController implements WalletApi {

    private final WalletService walletService;

    @Override
    public WalletResponse getWallet(final String organizerExternalKey, final UserPrincipal requestingUser) {
        requestingUser.assertCanManage(organizerExternalKey);
        return walletService.getWallet(organizerExternalKey);
    }

    @Override
    public PaginationResponse<WalletTransactionResponse> getTransactions(final String organizerExternalKey, final Pageable pageable, final UserPrincipal requestingUser) {
        requestingUser.assertCanManage(organizerExternalKey);
        return walletService.getTransactions(organizerExternalKey, pageable);
    }
}
