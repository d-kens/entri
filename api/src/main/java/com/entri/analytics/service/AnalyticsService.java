package com.entri.analytics.service;

import com.entri.analytics.TrendPeriod;
import com.entri.analytics.dto.OrganizerSummaryMetricsResponse;
import com.entri.analytics.dto.PlatformSummaryMetricsResponse;
import com.entri.analytics.dto.SalesTrendDataPoint;
import com.entri.analytics.dto.SalesTrendResponse;
import com.entri.events.entity.EventStatus;
import com.entri.users.entity.Role;
import com.entri.events.repository.EventRepository;
import com.entri.events.repository.EventTicketReservationRepository;
import com.entri.events.repository.TicketTypeRepository;
import com.entri.exception.ResourceNotFoundException;
import com.entri.users.repository.UserRepository;
import com.entri.wallet.WalletProvider;
import com.entri.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EventRepository eventRepository;
    private final EventTicketReservationRepository reservationRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final WalletProvider walletProvider;
    private final UserRepository userRepository;

    // No @Transactional here — each repository call uses Spring Data's own short tx,
    // so the JDBC connection is released before the wallet HTTP call.
    public OrganizerSummaryMetricsResponse getOrganizerSummaryMetrics(String organizerKey) {
        Instant now = Instant.now();

        BigDecimal totalRevenue = Objects.requireNonNullElse(
                reservationRepository.sumRevenueByOrganizer(organizerKey), BigDecimal.ZERO);
        long totalTicketsSold = ticketTypeRepository.sumSoldQuantityByOrganizer(organizerKey);
        long upcomingEventsCount = eventRepository.countUpcomingEvents(organizerKey, now);
        long liveEventsCount = eventRepository.countLiveEvents(organizerKey, now);

        var user = userRepository.findByExternalKeyAndDeletedFalse(organizerKey)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BigDecimal walletBalance = null;
        String walletCurrency = null;
        if (user.getWalletId() != null) {
            WalletResponse wallet = walletProvider.getWallet(user.getWalletId());
            walletBalance = wallet.availableBalance();
            walletCurrency = wallet.currency();
        }

        return new OrganizerSummaryMetricsResponse(
                totalRevenue,
                walletBalance,
                walletCurrency,
                totalTicketsSold,
                upcomingEventsCount,
                liveEventsCount
        );
    }

    @Transactional(readOnly = true)
    public SalesTrendResponse getOrganizerSalesTrend(String organizerKey, String periodStr) {
        TrendPeriod period = TrendPeriod.fromString(periodStr);
        Function<Instant, List<Object[]>> fetch = period.monthly
                ? from -> reservationRepository.findMonthlySalesTrend(organizerKey, from)
                : from -> reservationRepository.findDailySalesTrend(organizerKey, from);
        return buildTrend(period, fetch);
    }

    @Transactional(readOnly = true)
    public PlatformSummaryMetricsResponse getPlatformSummaryMetrics() {
        Instant now = Instant.now();
        return new PlatformSummaryMetricsResponse(
                userRepository.countByRole(Role.ORGANIZER),
                eventRepository.count(),
                eventRepository.countByStatus(EventStatus.PUBLISHED),
                eventRepository.countLiveEventsPlatform(now),
                ticketTypeRepository.sumSoldQuantityPlatform(),
                Objects.requireNonNullElse(reservationRepository.sumRevenuePlatform(), BigDecimal.ZERO)
        );
    }

    @Transactional(readOnly = true)
    public SalesTrendResponse getPlatformSalesTrend(String periodStr) {
        TrendPeriod period = TrendPeriod.fromString(periodStr);
        Function<Instant, List<Object[]>> fetch = period.monthly
                ? from -> reservationRepository.findMonthlySalesTrendPlatform(from)
                : from -> reservationRepository.findDailySalesTrendPlatform(from);
        return buildTrend(period, fetch);
    }

    private SalesTrendResponse buildTrend(TrendPeriod period, Function<Instant, List<Object[]>> fetch) {
        List<SalesTrendDataPoint> data = new ArrayList<>();

        if (period.monthly) {
            Instant from = YearMonth.now(ZoneOffset.UTC).minusMonths(period.count - 1L)
                    .atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            Map<String, SalesTrendDataPoint> byBucket = fetch.apply(from).stream()
                    .collect(Collectors.toMap(r -> r[0].toString(), this::toDataPoint));

            YearMonth cursor = YearMonth.now(ZoneOffset.UTC).minusMonths(period.count - 1L);
            YearMonth end = YearMonth.now(ZoneOffset.UTC);
            while (!cursor.isAfter(end)) {
                String key = cursor.toString();
                data.add(byBucket.getOrDefault(key, new SalesTrendDataPoint(key, 0, BigDecimal.ZERO)));
                cursor = cursor.plusMonths(1);
            }
        } else {
            Instant from = LocalDate.now(ZoneOffset.UTC).minusDays(period.count - 1L)
                    .atStartOfDay().toInstant(ZoneOffset.UTC);
            Map<String, SalesTrendDataPoint> byBucket = fetch.apply(from).stream()
                    .collect(Collectors.toMap(r -> r[0].toString(), this::toDataPoint));

            LocalDate cursor = LocalDate.now(ZoneOffset.UTC).minusDays(period.count - 1L);
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            while (!cursor.isAfter(today)) {
                String key = cursor.format(DateTimeFormatter.ISO_LOCAL_DATE);
                data.add(byBucket.getOrDefault(key, new SalesTrendDataPoint(key, 0, BigDecimal.ZERO)));
                cursor = cursor.plusDays(1);
            }
        }

        return new SalesTrendResponse(period.getValue(), data);
    }

    private SalesTrendDataPoint toDataPoint(Object[] r) {
        return new SalesTrendDataPoint(r[0].toString(), ((Number) r[1]).longValue(), new BigDecimal(r[2].toString()));
    }
}
