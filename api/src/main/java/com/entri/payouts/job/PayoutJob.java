package com.entri.payouts.job;

import com.entri.payouts.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutJob {

    private final PayoutService payoutService;

    // Default: every minute. Override via platform.payout.cron in per-env config.
    @Scheduled(cron = "${platform.payout.cron:0 * * * * *}")
    @SchedulerLock(name = "processPayouts", lockAtMostFor = "PT55S", lockAtLeastFor = "PT10S")
    public void processPayouts() {
        int processed = payoutService.processNextBatch();
        if (processed > 0) {
            log.info("Dispatched {} payout(s)", processed);
        }
    }
}
