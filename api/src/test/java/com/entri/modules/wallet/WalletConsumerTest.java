package com.entri.modules.wallet;

import com.entri.payment.PayoutResultEvent;
import com.entri.payment.enums.PayoutStatus;
import com.entri.tickets.dto.ReservationConfirmedEvent;
import com.entri.wallet.WalletConsumer;
import com.entri.wallet.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WalletConsumerTest {

    @Mock WalletService walletService;

    @InjectMocks WalletConsumer walletConsumer;

    @Test
    void onReservationConfirmed_creditsOrganizerWalletForReservation() {
        var event = new ReservationConfirmedEvent("res-ext-123", "org-ext-456", BigDecimal.TEN, "KES");

        walletConsumer.onReservationConfirmed(event);

        verify(walletService).creditReservation("org-ext-456", BigDecimal.TEN, "KES", "res-ext-123");
    }

    @Test
    void onPayoutResult_delegatesToWalletServiceWithTrackingIdAndStatus() {
        var event = new PayoutResultEvent("track-123", PayoutStatus.COMPLETED);

        walletConsumer.onPayoutResult(event);

        verify(walletService).applyPayoutResult("track-123", PayoutStatus.COMPLETED);
    }
}
