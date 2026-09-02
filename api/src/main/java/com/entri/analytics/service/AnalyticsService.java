package com.entri.analytics.service;

import com.entri.analytics.dto.OrganizerSummaryMetricsResponse;
import com.entri.analytics.dto.PlatformSummaryMetricsResponse;
import com.entri.analytics.dto.SalesTrendDataPoint;
import com.entri.analytics.dto.SalesTrendResponse;
import com.entri.events.entity.EventStatus;
import com.entri.users.entity.Role;
import com.entri.events.repository.EventRepository;
import com.entri.events.repository.EventTicketReservationRepository;
import com.entri.events.repository.TicketTypeRepository;
import com.entri.exception.BadRequestException;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EventRepository eventRepository;
    private final EventTicketReservationRepository reservationRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final WalletProvider walletProvider;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public OrganizerSummaryMetricsResponse getOrganizerSummaryMetrics(String organizerKey) {
        Instant now = Instant.now();

        BigDecimal totalRevenue = Objects.requireNonNullElse(
                reservationRepository.sumRevenueByOrganizer(organizerKey), BigDecimal.ZERO);
        long totalTicketsSold = ticketTypeRepository.sumSoldQuantityByOrganizer(organizerKey);
        long upcomingEventsCount = eventRepository.countUpcomingEvents(organizerKey, now);
        long liveEventsCount = eventRepository.countLiveEvents(organizerKey, now);

        BigDecimal walletBalance = null;
        String walletCurrency = null;
        var user = userRepository.findByExternalKeyAndDeletedFalse(organizerKey)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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
    public SalesTrendResponse getOrganizerSalesTrend(String organizerKey, String period) {
        return switch (period) {
            case "7d" -> buildDailyTrend(organizerKey, period, 7);
            case "30d" -> buildDailyTrend(organizerKey, period, 30);
            case "12m" -> buildMonthlyTrend(organizerKey, period, 12);
            default -> throw new BadRequestException("Invalid period. Allowed values: 7d, 30d, 12m");
        };
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
    public SalesTrendResponse getPlatformSalesTrend(String period) {
        return switch (period) {
            case "7d" -> buildDailyTrendPlatform(period, 7);
            case "30d" -> buildDailyTrendPlatform(period, 30);
            case "12m" -> buildMonthlyTrendPlatform(period, 12);
            default -> throw new BadRequestException("Invalid period. Allowed values: 7d, 30d, 12m");
        };
    }

    private SalesTrendResponse buildDailyTrend(String organizerKey, String period, int days) {
        Instant from = LocalDate.now(ZoneOffset.UTC).minusDays(days - 1L).atStartOfDay().toInstant(ZoneOffset.UTC);
        List<Object[]> rows = reservationRepository.findDailySalesTrend(organizerKey, from);

        Map<String, SalesTrendDataPoint> byDate = rows.stream()
                .collect(Collectors.toMap(
                        r -> r[0].toString(),
                        r -> new SalesTrendDataPoint(r[0].toString(), ((Number) r[1]).longValue(), new BigDecimal(r[2].toString()))
                ));

        List<SalesTrendDataPoint> data = new ArrayList<>();
        LocalDate cursor = LocalDate.now(ZoneOffset.UTC).minusDays(days - 1L);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        while (!cursor.isAfter(today)) {
            String key = cursor.format(DateTimeFormatter.ISO_LOCAL_DATE);
            data.add(byDate.getOrDefault(key, new SalesTrendDataPoint(key, 0, BigDecimal.ZERO)));
            cursor = cursor.plusDays(1);
        }
        return new SalesTrendResponse(period, data);
    }

    private SalesTrendResponse buildMonthlyTrend(String organizerKey, String period, int months) {
        Instant from = YearMonth.now(ZoneOffset.UTC).minusMonths(months - 1L).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        List<Object[]> rows = reservationRepository.findMonthlySalesTrend(organizerKey, from);

        Map<String, SalesTrendDataPoint> byMonth = rows.stream()
                .collect(Collectors.toMap(
                        r -> r[0].toString(),
                        r -> new SalesTrendDataPoint(r[0].toString(), ((Number) r[1]).longValue(), new BigDecimal(r[2].toString()))
                ));

        List<SalesTrendDataPoint> data = new ArrayList<>();
        YearMonth cursor = YearMonth.now(ZoneOffset.UTC).minusMonths(months - 1L);
        YearMonth current = YearMonth.now(ZoneOffset.UTC);
        while (!cursor.isAfter(current)) {
            String key = cursor.toString();
            data.add(byMonth.getOrDefault(key, new SalesTrendDataPoint(key, 0, BigDecimal.ZERO)));
            cursor = cursor.plusMonths(1);
        }
        return new SalesTrendResponse(period, data);
    }

    private SalesTrendResponse buildDailyTrendPlatform(String period, int days) {
        Instant from = LocalDate.now(ZoneOffset.UTC).minusDays(days - 1L).atStartOfDay().toInstant(ZoneOffset.UTC);
        List<Object[]> rows = reservationRepository.findDailySalesTrendPlatform(from);

        Map<String, SalesTrendDataPoint> byDate = rows.stream()
                .collect(Collectors.toMap(
                        r -> r[0].toString(),
                        r -> new SalesTrendDataPoint(r[0].toString(), ((Number) r[1]).longValue(), new BigDecimal(r[2].toString()))
                ));

        List<SalesTrendDataPoint> data = new ArrayList<>();
        LocalDate cursor = LocalDate.now(ZoneOffset.UTC).minusDays(days - 1L);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        while (!cursor.isAfter(today)) {
            String key = cursor.format(DateTimeFormatter.ISO_LOCAL_DATE);
            data.add(byDate.getOrDefault(key, new SalesTrendDataPoint(key, 0, BigDecimal.ZERO)));
            cursor = cursor.plusDays(1);
        }
        return new SalesTrendResponse(period, data);
    }

    private SalesTrendResponse buildMonthlyTrendPlatform(String period, int months) {
        Instant from = YearMonth.now(ZoneOffset.UTC).minusMonths(months - 1L).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        List<Object[]> rows = reservationRepository.findMonthlySalesTrendPlatform(from);

        Map<String, SalesTrendDataPoint> byMonth = rows.stream()
                .collect(Collectors.toMap(
                        r -> r[0].toString(),
                        r -> new SalesTrendDataPoint(r[0].toString(), ((Number) r[1]).longValue(), new BigDecimal(r[2].toString()))
                ));

        List<SalesTrendDataPoint> data = new ArrayList<>();
        YearMonth cursor = YearMonth.now(ZoneOffset.UTC).minusMonths(months - 1L);
        YearMonth current = YearMonth.now(ZoneOffset.UTC);
        while (!cursor.isAfter(current)) {
            String key = cursor.toString();
            data.add(byMonth.getOrDefault(key, new SalesTrendDataPoint(key, 0, BigDecimal.ZERO)));
            cursor = cursor.plusMonths(1);
        }
        return new SalesTrendResponse(period, data);
    }
}
