package com.entri.modules.wallet;

import com.entri.common.PlatformProperties;
import com.entri.exception.BadRequestException;
import com.entri.exception.ForbiddenException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.payment.PaymentGateway;
import com.entri.payment.dto.PayoutRequest;
import com.entri.payment.enums.AccountType;
import com.entri.payment.enums.PayoutStatus;
import com.entri.security.UserPrincipal;
import com.entri.users.entity.Role;
import com.entri.users.entity.User;
import com.entri.users.repository.UserRepository;
import com.entri.wallet.WalletService;
import com.entri.wallet.WalletTransactionStatus;
import com.entri.wallet.WalletTransactionType;
import com.entri.wallet.WalletType;
import com.entri.wallet.dto.WithdrawalRequest;
import com.entri.wallet.entity.Wallet;
import com.entri.wallet.entity.WalletTransaction;
import com.entri.wallet.repository.WalletRepository;
import com.entri.wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock WalletRepository walletRepository;
    @Mock WalletTransactionRepository walletTransactionRepository;
    @Mock UserRepository userRepository;
    @Mock PaymentGateway paymentGateway;
    @Mock PlatformTransactionManager transactionManager;

    @org.mockito.Spy PlatformProperties platformProperties = new PlatformProperties(BigDecimal.valueOf(0.1));

    @InjectMocks WalletService walletService;

    private User buildOrganizer(String externalKey) {
        return User.builder().externalKey(externalKey).role(Role.ORGANIZER).build();
    }

    private Wallet buildWallet(String externalId, WalletType type, BigDecimal balance) {
        return Wallet.builder()
                .id(1L)
                .externalId(externalId)
                .walletType(type)
                .balance(balance)
                .build();
    }

    @Test
    void getPlatformWallet_notFound_throwsResourceNotFoundException() {
        when(walletRepository.findByWalletType(WalletType.PLATFORM)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.getPlatformWallet())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPlatformWallet_found_returnsResponse() {
        var wallet = buildWallet("wallet-ext", WalletType.PLATFORM, BigDecimal.TEN);
        when(walletRepository.findByWalletType(WalletType.PLATFORM)).thenReturn(Optional.of(wallet));

        var result = walletService.getPlatformWallet();

        assertThat(result.externalId()).isEqualTo("wallet-ext");
        assertThat(result.balance()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    void getWallet_existingWallet_returnsResponseWithoutCreating() {
        var wallet = buildWallet("wallet-ext", WalletType.ORGANIZER, BigDecimal.ZERO);
        when(walletRepository.findByOrganizerExternalKey("org-key")).thenReturn(Optional.of(wallet));

        var result = walletService.getWallet("org-key");

        assertThat(result.externalId()).isEqualTo("wallet-ext");
        verify(userRepository, never()).findByExternalKeyAndDeletedFalse(any());
    }

    @Test
    void getWallet_noExistingWallet_createsWalletForOrganizer() {
        var organizer = buildOrganizer("org-key");
        var savedWallet = buildWallet("new-wallet-ext", WalletType.ORGANIZER, BigDecimal.ZERO);

        when(walletRepository.findByOrganizerExternalKey("org-key")).thenReturn(Optional.empty());
        when(userRepository.findByExternalKeyAndDeletedFalse("org-key")).thenReturn(Optional.of(organizer));
        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        var result = walletService.getWallet("org-key");

        assertThat(result.externalId()).isEqualTo("new-wallet-ext");
    }

    @Test
    void getWallet_organizerNotFound_throwsResourceNotFoundException() {
        when(walletRepository.findByOrganizerExternalKey("missing")).thenReturn(Optional.empty());
        when(userRepository.findByExternalKeyAndDeletedFalse("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.getWallet("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getTransactions_returnsMappedPaginationResponse() {
        var wallet = buildWallet("wallet-ext", WalletType.ORGANIZER, BigDecimal.ZERO);
        var transaction = WalletTransaction.builder()
                .externalId("txn-ext")
                .wallet(wallet)
                .type(WalletTransactionType.CREDIT)
                .amount(BigDecimal.valueOf(50))
                .currency("KES")
                .referenceId("ref-1")
                .status(WalletTransactionStatus.COMPLETED)
                .build();
        var pageable = PageRequest.of(0, 10);
        Page<WalletTransaction> page = new PageImpl<>(List.of(transaction), pageable, 1);

        when(walletTransactionRepository.findByWalletOrganizerExternalKey("org-key", pageable)).thenReturn(page);

        var result = walletService.getTransactions("org-key", pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().externalId()).isEqualTo("txn-ext");
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void withdraw_fromPlatformWalletAsNonAdmin_throwsForbiddenException() {
        var wallet = buildWallet("wallet-ext", WalletType.PLATFORM, BigDecimal.valueOf(1000));
        var requestingUser = new UserPrincipal(buildOrganizer("org-key"));
        var request = new WithdrawalRequest(BigDecimal.valueOf(500), AccountType.PAYBILL, "12345", "John", "payout", null, null);

        when(walletRepository.findByExternalId("wallet-ext")).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.withdraw("wallet-ext", request, requestingUser))
                .isInstanceOf(ForbiddenException.class);

        verify(paymentGateway, never()).payout(any());
    }

    @Test
    void withdraw_fromOtherOrganizersWallet_throwsForbiddenException() {
        var owner = buildOrganizer("owner-key");
        var wallet = Wallet.builder().id(1L).externalId("wallet-ext").walletType(WalletType.ORGANIZER)
                .organizer(owner).balance(BigDecimal.valueOf(1000)).build();
        var requestingUser = new UserPrincipal(buildOrganizer("other-key"));
        var request = new WithdrawalRequest(BigDecimal.valueOf(500), AccountType.PAYBILL, "12345", "John", "payout", null, null);

        when(walletRepository.findByExternalId("wallet-ext")).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.withdraw("wallet-ext", request, requestingUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void withdraw_insufficientBalance_throwsBadRequestException() {
        var owner = buildOrganizer("owner-key");
        var wallet = Wallet.builder().id(1L).externalId("wallet-ext").walletType(WalletType.ORGANIZER)
                .organizer(owner).balance(BigDecimal.valueOf(100)).build();
        var requestingUser = new UserPrincipal(owner);
        var request = new WithdrawalRequest(BigDecimal.valueOf(500), AccountType.PAYBILL, "12345", "John", "payout", null, null);

        when(walletRepository.findByExternalId("wallet-ext")).thenReturn(Optional.of(wallet));
        when(walletRepository.decrementBalance(1L, BigDecimal.valueOf(500))).thenReturn(0);

        assertThatThrownBy(() -> walletService.withdraw("wallet-ext", request, requestingUser))
                .isInstanceOf(BadRequestException.class);

        verify(paymentGateway, never()).payout(any());
    }

    @Test
    void withdraw_success_callsPaymentGatewayAndReturnsPendingResponse() {
        var owner = buildOrganizer("owner-key");
        var wallet = Wallet.builder().id(1L).externalId("wallet-ext").walletType(WalletType.ORGANIZER)
                .organizer(owner).balance(BigDecimal.valueOf(1000)).build();
        var requestingUser = new UserPrincipal(owner);
        var request = new WithdrawalRequest(BigDecimal.valueOf(500), AccountType.PAYBILL, "12345", "John", "payout", "ref", "bank-code");

        when(walletRepository.findByExternalId("wallet-ext")).thenReturn(Optional.of(wallet));
        when(walletRepository.decrementBalance(1L, BigDecimal.valueOf(500))).thenReturn(1);
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentGateway.payout(any(PayoutRequest.class))).thenReturn("tracking-123");

        var result = walletService.withdraw("wallet-ext", request, requestingUser);

        assertThat(result.status()).isEqualTo(WalletTransactionStatus.PENDING);

        var captor = ArgumentCaptor.forClass(PayoutRequest.class);
        verify(paymentGateway).payout(captor.capture());
        assertThat(captor.getValue().amount()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(captor.getValue().accountType()).isEqualTo(AccountType.PAYBILL);

        verify(walletTransactionRepository, org.mockito.Mockito.times(2)).save(any(WalletTransaction.class));
    }

    @Test
    void withdraw_paymentGatewayFails_marksTransactionFailedAndRefundsBalanceThenRethrows() {
        var owner = buildOrganizer("owner-key");
        var wallet = Wallet.builder().id(1L).externalId("wallet-ext").walletType(WalletType.ORGANIZER)
                .organizer(owner).balance(BigDecimal.valueOf(1000)).build();
        var requestingUser = new UserPrincipal(owner);
        var request = new WithdrawalRequest(BigDecimal.valueOf(500), AccountType.PAYBILL, "12345", "John", "payout", null, null);

        when(walletRepository.findByExternalId("wallet-ext")).thenReturn(Optional.of(wallet));
        when(walletRepository.decrementBalance(1L, BigDecimal.valueOf(500))).thenReturn(1);
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentGateway.payout(any(PayoutRequest.class))).thenThrow(new RuntimeException("gateway down"));

        assertThatThrownBy(() -> walletService.withdraw("wallet-ext", request, requestingUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("gateway down");

        var captor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(WalletTransactionStatus.FAILED);
        verify(walletRepository).incrementBalance(eq(1L), eq(BigDecimal.valueOf(500)));
    }

    @Test
    void applyPayoutResult_unknownTrackingId_doesNothing() {
        when(walletTransactionRepository.findByTrackingReference("unknown")).thenReturn(Optional.empty());

        walletService.applyPayoutResult("unknown", PayoutStatus.COMPLETED);

        verify(walletTransactionRepository, never()).save(any());
    }

    @Test
    void applyPayoutResult_transactionAlreadyProcessed_doesNothing() {
        var wallet = buildWallet("wallet-ext", WalletType.ORGANIZER, BigDecimal.ZERO);
        var transaction = WalletTransaction.builder()
                .wallet(wallet).status(WalletTransactionStatus.COMPLETED)
                .amount(BigDecimal.TEN).currency("KES").referenceId("ref").type(WalletTransactionType.DEBIT)
                .build();

        when(walletTransactionRepository.findByTrackingReference("tracking-1")).thenReturn(Optional.of(transaction));

        walletService.applyPayoutResult("tracking-1", PayoutStatus.COMPLETED);

        verify(walletTransactionRepository, never()).save(any());
    }

    @Test
    void applyPayoutResult_completed_marksTransactionCompleted() {
        var wallet = buildWallet("wallet-ext", WalletType.ORGANIZER, BigDecimal.ZERO);
        var transaction = WalletTransaction.builder()
                .wallet(wallet).status(WalletTransactionStatus.PENDING)
                .amount(BigDecimal.TEN).currency("KES").referenceId("ref").type(WalletTransactionType.DEBIT)
                .build();

        when(walletTransactionRepository.findByTrackingReference("tracking-1")).thenReturn(Optional.of(transaction));

        walletService.applyPayoutResult("tracking-1", PayoutStatus.COMPLETED);

        assertThat(transaction.getStatus()).isEqualTo(WalletTransactionStatus.COMPLETED);
        verify(walletRepository, never()).incrementBalance(any(), any());
        verify(walletTransactionRepository).save(transaction);
    }

    @Test
    void applyPayoutResult_failed_marksTransactionFailedAndRefundsBalance() {
        var wallet = buildWallet("wallet-ext", WalletType.ORGANIZER, BigDecimal.ZERO);
        var transaction = WalletTransaction.builder()
                .wallet(wallet).status(WalletTransactionStatus.PENDING)
                .amount(BigDecimal.TEN).currency("KES").referenceId("ref").type(WalletTransactionType.DEBIT)
                .build();

        when(walletTransactionRepository.findByTrackingReference("tracking-1")).thenReturn(Optional.of(transaction));

        walletService.applyPayoutResult("tracking-1", PayoutStatus.FAILED);

        assertThat(transaction.getStatus()).isEqualTo(WalletTransactionStatus.FAILED);
        verify(walletRepository).incrementBalance(1L, BigDecimal.TEN);
        verify(walletTransactionRepository).save(transaction);
    }

    @Test
    void creditReservation_splitsPlatformFeeFromOrganizerAmount() {
        var organizer = buildOrganizer("org-key");
        var organizerWallet = buildWallet("org-wallet", WalletType.ORGANIZER, BigDecimal.ZERO);
        var platformWallet = buildWallet("platform-wallet", WalletType.PLATFORM, BigDecimal.ZERO);

        when(walletRepository.findByOrganizerExternalKey("org-key")).thenReturn(Optional.of(organizerWallet));
        when(walletRepository.findByWalletType(WalletType.PLATFORM)).thenReturn(Optional.of(platformWallet));

        walletService.creditReservation("org-key", BigDecimal.valueOf(100), "KES", "ref-1");

        var captor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionRepository, org.mockito.Mockito.times(2)).save(captor.capture());

        var amounts = captor.getAllValues().stream().map(WalletTransaction::getAmount).toList();
        assertThat(amounts).containsExactlyInAnyOrder(BigDecimal.valueOf(90.00).setScale(2), BigDecimal.valueOf(10.00).setScale(2));
    }

    @Test
    void credit_walletNotFound_throwsResourceNotFoundException() {
        when(walletRepository.findByOrganizerExternalKey("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.credit("missing", BigDecimal.TEN, "KES", "ref-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void credit_success_incrementsBalanceAndSavesTransaction() {
        var wallet = buildWallet("wallet-ext", WalletType.ORGANIZER, BigDecimal.ZERO);
        when(walletRepository.findByOrganizerExternalKey("org-key")).thenReturn(Optional.of(wallet));

        walletService.credit("org-key", BigDecimal.TEN, "KES", "ref-1");

        verify(walletRepository).incrementBalance(1L, BigDecimal.TEN);
        verify(walletTransactionRepository).save(any(WalletTransaction.class));
    }
}
