package com.entri.tickets.service;

import com.entri.events.dto.EventCheckInStatsResponse;
import com.entri.events.dto.RecentCheckInDto;
import com.entri.events.entity.EventTicketReservation;
import com.entri.events.repository.EventRepository;
import com.entri.events.repository.EventTicketReservationRepository;
import com.entri.exception.BadRequestException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.notification.NotificationEventPublisher;
import com.entri.notification.NotificationType;
import com.entri.notification.dto.NotificationEvent;
import com.entri.security.UserPrincipal;
import com.entri.tickets.dto.CheckInRequest;
import com.entri.tickets.dto.CheckInResponse;
import com.entri.tickets.dto.CheckInResult;
import com.entri.tickets.dto.TicketResponse;
import com.entri.tickets.entity.Ticket;
import com.entri.tickets.entity.TicketStatus;
import com.entri.tickets.mapper.TicketMapper;
import com.entri.tickets.repository.EventCheckInCodeRepository;
import com.entri.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventTicketReservationRepository reservationRepository;
    private final EventCheckInCodeRepository checkInCodeRepository;
    private final EventRepository eventRepository;
    private final TicketMapper ticketMapper;
    private final NotificationEventPublisher notificationEventPublisher;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Transactional(readOnly = true)
    public EventCheckInStatsResponse getCheckInStats(String eventExternalId, UserPrincipal requestingUser) {
        var event = eventRepository.findByExternalId(eventExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Event with ID " + eventExternalId + " not found"));
        requestingUser.assertCanManage(event.getOrganizer().getExternalKey());

        long total = ticketRepository.countByEventExternalId(eventExternalId);
        long checkedIn = ticketRepository.countCheckedInByEventExternalId(eventExternalId);
        double checkInPercentage = total == 0 ? 0.0 : (double) checkedIn / total * 100;

        List<RecentCheckInDto> recentCheckIns = ticketRepository
                .findRecentCheckIns(eventExternalId, PageRequest.of(0, 10))
                .stream()
                .map(t -> new RecentCheckInDto(t.getTicketCode(), t.getTicketType().getName(), t.getCheckedInAt()))
                .toList();

        return new EventCheckInStatsResponse(eventExternalId, total, checkedIn, checkInPercentage, recentCheckIns);
    }

    @Transactional
    public void generateTickets(String reservationExternalId) {
        var reservation = reservationRepository.findByExternalIdWithItems(reservationExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + reservationExternalId));

        if (ticketRepository.existsByReservationId(reservation.getId())) {
            return;
        }

        var tickets = new ArrayList<Ticket>();
        for (var item : reservation.getItems()) {
            for (int i = 0; i < item.getQuantity(); i++) {
                tickets.add(Ticket.builder()
                        .reservation(reservation)
                        .event(reservation.getEvent())
                        .ticketType(item.getTicketType())
                        .build());
            }
        }

        ticketRepository.saveAll(tickets);
        sendConfirmationEmail(reservation);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByReservation(String reservationExternalId) {
        return ticketRepository.findByReservationExternalId(reservationExternalId)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicket(String externalId) {
        var ticket = ticketRepository.findByExternalIdWithDetails(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + externalId));
        return ticketMapper.toResponse(ticket);
    }

    @Transactional
    public CheckInResponse checkIn(String ticketCode, CheckInRequest request) {
        var code = checkInCodeRepository.findValidCode(request.checkInCode().trim().toUpperCase(), Instant.now())
                .orElseThrow(() -> new BadRequestException("Invalid or expired check-in code"));

        var ticket = ticketRepository.findByTicketCodeWithDetails(ticketCode).orElse(null);

        if (ticket == null || !ticket.getEvent().getId().equals(code.getEvent().getId())) {
            return new CheckInResponse(CheckInResult.INVALID, null, null, null);
        }

        if (ticket.getStatus() == TicketStatus.USED) {
            return new CheckInResponse(
                    CheckInResult.ALREADY_USED,
                    holderName(ticket),
                    ticket.getTicketType().getName(),
                    ticket.getCheckedInAt()
            );
        }

        int updated = ticketRepository.markAsUsed(ticketCode, Instant.now());
        if (updated == 0) {
            var refreshed = ticketRepository.findByTicketCodeWithDetails(ticketCode).orElseThrow();
            return new CheckInResponse(
                    CheckInResult.ALREADY_USED,
                    holderName(refreshed),
                    refreshed.getTicketType().getName(),
                    refreshed.getCheckedInAt()
            );
        }

        return new CheckInResponse(
                CheckInResult.VALID,
                holderName(ticket),
                ticket.getTicketType().getName(),
                Instant.now()
        );
    }

    private String holderName(Ticket ticket) {
        var r = ticket.getReservation();
        return Stream.of(r.getFirstName(), r.getLastName())
                .filter(s -> s != null)
                .collect(Collectors.joining(" "));
    }

    private void sendConfirmationEmail(EventTicketReservation reservation) {
        String ticketsUrl = appBaseUrl.stripTrailing() + "/tickets/" + reservation.getExternalId();
        var payload = new HashMap<String, Object>();
        payload.put("firstName", reservation.getFirstName());
        payload.put("eventName", reservation.getEvent().getTitle());
        payload.put("ticketsUrl", ticketsUrl);

        notificationEventPublisher.publish(new NotificationEvent(
                NotificationType.TICKET_CONFIRMATION,
                reservation.getExternalId(),
                payload
        ));
    }
}
