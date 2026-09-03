package com.entri.analytics.service;

import com.entri.analytics.dto.OrganizerSummaryMetricsResponse;
import com.entri.events.repository.EventRepository;
import com.entri.events.repository.EventTicketReservationRepository;
import com.entri.events.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EventRepository eventRepository;
    private final EventTicketReservationRepository reservationRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Transactional(readOnly = true)
    public OrganizerSummaryMetricsResponse getOrganizerSummaryMetrics(String organizerKey) {
        Instant now = Instant.now();
        return new OrganizerSummaryMetricsResponse(
                Objects.requireNonNullElse(reservationRepository.sumRevenueByOrganizer(organizerKey), BigDecimal.ZERO),
                ticketTypeRepository.sumSoldQuantityByOrganizer(organizerKey),
                eventRepository.countUpcomingEvents(organizerKey, now),
                eventRepository.countLiveEvents(organizerKey, now)
        );
    }

}
