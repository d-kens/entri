package com.entri.events.job;

import com.entri.events.service.EventTicketReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpirationJob {
    private final EventTicketReservationService eventTicketReservationService;

    @Scheduled(cron = "${events.reservation.expiration.cron:0 */2 * * * *}")
    @SchedulerLock(name = "expireReservations", lockAtMostFor = "PT1M", lockAtLeastFor = "PT30S")
    public void expireReservations() {
        int total = 0;
        int expired;
        do {
            expired = eventTicketReservationService.expireReservations();
            total += expired;
        } while (expired > 0);
        if (total > 0) log.info("Expired {} reservations", total);
    }
}
