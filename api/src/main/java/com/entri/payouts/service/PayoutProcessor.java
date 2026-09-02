package com.entri.payouts.service;

import com.entri.intasend.IntaSendClient;
import com.entri.intasend.dto.IntaSendSendMoneyRequest;
import com.entri.intasend.dto.IntaSendTransactionItem;
import com.entri.payouts.entity.Payout;
import com.entri.payouts.entity.PayoutStatus;
import com.entri.payouts.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutProcessor {

    private final PayoutRepository payoutRepository;
    private final IntaSendClient intaSendClient;

    @Value("${platform.payout.max-attempts:3}")
    private int maxAttempts;

    @Value("${platform.payout.batch-size:50}")
    private int batchSize;

    public int processNextBatch() {
        List<Payout> pending = payoutRepository.findByStatusAndAttemptsLessThan(
                PayoutStatus.PENDING, maxAttempts, PageRequest.of(0, batchSize));

        for (Payout payout : pending) {
            process(payout);
        }

        return pending.size();
    }

    private void process(Payout payout) {
        payout.setAttempts(payout.getAttempts() + 1);
        payout.setLastAttemptedAt(Instant.now());

        try {
            var item = new IntaSendTransactionItem(
                    payout.getPayoutRecipientName(),
                    payout.getPayoutAccount(),
                    payout.getPayoutAccountReference(),
                    payout.getPayoutBankCode(),
                    payout.getAmount(),
                    "Organizer payout " + payout.getIdempotencyKey()
            );

            String uri = switch (payout.getPayoutMethod()) {
                case MPESA_PAYBILL, MPESA_TILL -> "/api/v1/send-money/mpesa/";
                case BANK -> "/api/v1/send-money/bank/";
            };

            intaSendClient.sendMoney(uri, new IntaSendSendMoneyRequest(
                    payout.getCurrency(), List.of(item), null, "NO"
            ));

            payout.setStatus(PayoutStatus.COMPLETED);
            log.info("Payout {} completed — {} {} via {}",
                    payout.getIdempotencyKey(), payout.getCurrency(), payout.getAmount(), payout.getPayoutMethod());

        } catch (Exception e) {
            log.error("Payout {} failed on attempt {}: {}",
                    payout.getIdempotencyKey(), payout.getAttempts(), e.getMessage());

            if (payout.getAttempts() >= maxAttempts) {
                payout.setStatus(PayoutStatus.FAILED);
                log.error("Payout {} permanently failed after {} attempts — manual intervention required for reservation {}",
                        payout.getIdempotencyKey(), payout.getAttempts(),
                        payout.getReservation().getExternalId());
            }
        }

        payoutRepository.save(payout);
    }
}
