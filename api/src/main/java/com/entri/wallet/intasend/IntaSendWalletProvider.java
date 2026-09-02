package com.entri.wallet.intasend;

import com.entri.common.dto.PaginationResponse;
import com.entri.intasend.IntaSendClient;
import com.entri.intasend.dto.IntaSendSendMoneyRequest;
import com.entri.intasend.dto.IntaSendTransactionItem;
import com.entri.intasend.dto.IntaSendWalletRequest;
import com.entri.wallet.WalletProvider;
import com.entri.wallet.dto.BankCodeResponse;
import com.entri.wallet.dto.WalletTransactionResponse;
import com.entri.wallet.dto.WithdrawRequest;
import com.entri.wallet.dto.WithdrawResponse;
import com.entri.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IntaSendWalletProvider implements WalletProvider {

    private final IntaSendClient intaSendClient;

    @Override
    public String createWallet(final String label) {
        var request = new IntaSendWalletRequest("WORKING", "KES", label, true);
        var response = intaSendClient.createWallet(request);
        return response.walletId();
    }

    @Override
    public WalletResponse getWallet(final String walletId) {
        var response = intaSendClient.getWallet(walletId);
        return new WalletResponse(
                response.walletId(),
                response.label(),
                response.currency(),
                response.currentBalance(),
                response.availableBalance()
        );
    }

    @Override
    public WithdrawResponse withdraw(final WithdrawRequest request, final String walletId) {
        var item = new IntaSendTransactionItem(
                request.recipientName(),
                request.account(),
                request.accountNumber(),
                request.bankCode(),
                request.amount(),
                "Wallet withdrawal"
        );
        var sendMoneyRequest = new IntaSendSendMoneyRequest("KES", List.of(item), walletId, "NO");

        String uri = switch (request.type()) {
            case MPESA_PAYBILL -> "/api/v1/send-money/mpesa/";
            case BANK -> "/api/v1/send-money/bank/";
        };

        var response = intaSendClient.sendMoney(uri, sendMoneyRequest);
        return new WithdrawResponse(response.id(), response.status());
    }

    @Override
    public List<BankCodeResponse> getBankCodes() {
        var response = intaSendClient.getBankCodes();
        return response.results().stream()
                .map(b -> new BankCodeResponse(b.bankId(), b.bankName()))
                .toList();
    }

    @Override
    public PaginationResponse<WalletTransactionResponse> getTransactions(final String walletId, final int page, final int pageSize) {
        var response = intaSendClient.getWalletTransactions(walletId, page, pageSize);
        var transactions = response.results().stream()
                .map(t -> new WalletTransactionResponse(
                        t.id(),
                        t.transactionType(),
                        t.amount(),
                        t.currency(),
                        t.narrative(),
                        t.status(),
                        t.createdAt()
                ))
                .toList();

        int totalPages = (pageSize > 0) ? (int) Math.ceil((double) response.count() / pageSize) : 1;
        return new PaginationResponse<>(
                transactions,
                page,
                pageSize,
                response.count(),
                totalPages,
                page == 1,
                response.next() == null
        );
    }
}
