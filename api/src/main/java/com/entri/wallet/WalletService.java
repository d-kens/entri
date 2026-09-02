package com.entri.wallet;

import com.entri.common.dto.PaginationResponse;
import com.entri.exception.BadRequestException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.exception.UnauthorizedException;
import com.entri.security.UserPrincipal;
import com.entri.users.entity.User;
import com.entri.users.repository.UserRepository;
import com.entri.wallet.dto.BankCodeResponse;
import com.entri.wallet.dto.WalletTransactionResponse;
import com.entri.wallet.dto.WithdrawRequest;
import com.entri.wallet.dto.WithdrawResponse;
import com.entri.wallet.dto.WithdrawType;
import com.entri.wallet.dto.WalletResponse;
import com.entri.wallet.exception.InsufficientBalanceException;
import com.entri.wallet.exception.WalletAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletProvider walletProvider;
    private final UserRepository userRepository;

    @Transactional
    public WalletResponse createWalletForUser(final String externalKey) {
        var user = userRepository.findByExternalKeyAndDeletedFalse(externalKey)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getWalletId() != null) {
            throw new WalletAlreadyExistsException();
        }
        provisionWallet(user);
        return walletProvider.getWallet(user.getWalletId());
    }

    @Transactional
    public void provisionWalletForOrganizer(final String externalKey) {
        var user = userRepository.findByExternalKeyAndDeletedFalse(externalKey)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getWalletId() != null) {
            return;
        }
        provisionWallet(user);
    }

    @Transactional
    public void provisionWallet(final User user) {
        String sanitizedLabel = (user.getFirstName() + " " + user.getLastName())
                .replaceAll("[^a-zA-Z0-9_\\- ]", "").strip();
        String walletId = walletProvider.createWallet(sanitizedLabel);
        user.setWalletId(walletId);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(final String externalKey, final UserPrincipal requestingUser) {
        assertCallerAuthorized(externalKey, requestingUser);
        return walletProvider.getWallet(resolveWalletId(externalKey));
    }

    public WithdrawResponse withdraw(final String externalKey, final WithdrawRequest request, final UserPrincipal requestingUser) {
        assertCallerAuthorized(externalKey, requestingUser);
        validateWithdrawRequest(request);

        String walletId = resolveWalletId(externalKey);
        var wallet = walletProvider.getWallet(walletId);
        if (wallet.availableBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException();
        }

        return walletProvider.withdraw(request, walletId);
    }

    public List<BankCodeResponse> getBankCodes() {
        return walletProvider.getBankCodes();
    }

    public PaginationResponse<WalletTransactionResponse> getTransactions(
            final String externalKey, final int page, final int pageSize, final UserPrincipal requestingUser) {
        assertCallerAuthorized(externalKey, requestingUser);
        return walletProvider.getTransactions(resolveWalletId(externalKey), page, pageSize);
    }

    private void assertCallerAuthorized(final String externalKey, final UserPrincipal requestingUser) {
        if (!requestingUser.isAdmin() && !requestingUser.getExternalKey().equals(externalKey)) {
            throw new UnauthorizedException("You are not authorized to perform this action");
        }
    }

    private String resolveWalletId(final String externalKey) {
        var user = userRepository.findByExternalKeyAndDeletedFalse(externalKey)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getWalletId() == null) {
            throw new ResourceNotFoundException("Wallet not found");
        }
        return user.getWalletId();
    }

    private void validateWithdrawRequest(final WithdrawRequest request) {
        if (request.type() == WithdrawType.MPESA_PAYBILL) {
            if (request.accountNumber() == null || request.accountNumber().isBlank()) {
                throw new BadRequestException("Account number is required for M-Pesa Paybill withdrawals");
            }
        } else if (request.type() == WithdrawType.BANK) {
            if (request.bankCode() == null || request.bankCode().isBlank()) {
                throw new BadRequestException("Bank code is required for bank withdrawals");
            }
        }
    }
}
