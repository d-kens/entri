package com.entri.modules.analytics.service;

import com.entri.analytics.service.AnalyticsService;
import com.entri.events.repository.EventRepository;
import com.entri.events.repository.TicketTypeRepository;
import com.entri.wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    private static final String ORGANIZER_KEY = "organizer-key";

    @Mock EventRepository eventRepository;
    @Mock WalletTransactionRepository walletTransactionRepository;
    @Mock TicketTypeRepository ticketTypeRepository;

    @InjectMocks AnalyticsService analyticsService;

    @Test
    void getOrganizerSummaryMetrics_noRevenueYet_defaultsToZero() {
        when(walletTransactionRepository.sumRevenueByOrganizer(ORGANIZER_KEY)).thenReturn(null);
        when(ticketTypeRepository.sumSoldQuantityByOrganizer(ORGANIZER_KEY)).thenReturn(0L);
        when(eventRepository.countUpcomingEvents(eq(ORGANIZER_KEY), any())).thenReturn(0L);
        when(eventRepository.countLiveEvents(eq(ORGANIZER_KEY), any())).thenReturn(0L);

        var result = analyticsService.getOrganizerSummaryMetrics(ORGANIZER_KEY);

        assertThat(result.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalTicketsSold()).isZero();
        assertThat(result.upcomingEventsCount()).isZero();
        assertThat(result.liveEventsCount()).isZero();
    }

    @Test
    void getOrganizerSummaryMetrics_withActivity_returnsAggregatedMetrics() {
        when(walletTransactionRepository.sumRevenueByOrganizer(ORGANIZER_KEY)).thenReturn(BigDecimal.valueOf(1500));
        when(ticketTypeRepository.sumSoldQuantityByOrganizer(ORGANIZER_KEY)).thenReturn(42L);
        when(eventRepository.countUpcomingEvents(eq(ORGANIZER_KEY), any())).thenReturn(3L);
        when(eventRepository.countLiveEvents(eq(ORGANIZER_KEY), any())).thenReturn(1L);

        var result = analyticsService.getOrganizerSummaryMetrics(ORGANIZER_KEY);

        assertThat(result.totalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(1500));
        assertThat(result.totalTicketsSold()).isEqualTo(42L);
        assertThat(result.upcomingEventsCount()).isEqualTo(3L);
        assertThat(result.liveEventsCount()).isEqualTo(1L);
    }
}
