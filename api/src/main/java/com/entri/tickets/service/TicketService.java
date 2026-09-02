package com.entri.tickets.service;

import com.entri.events.entity.EventTicketReservation;
import com.entri.events.repository.EventTicketReservationRepository;
import com.entri.exception.BadRequestException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.notification.NotificationEventPublisher;
import com.entri.notification.NotificationType;
import com.entri.notification.dto.NotificationMessage;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventTicketReservationRepository reservationRepository;
    private final EventCheckInCodeRepository checkInCodeRepository;
    private final TicketMapper ticketMapper;
    private final NotificationEventPublisher notificationPublisher;

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
    public TicketResponse getTicket(String ticketCode) {
        var ticket = ticketRepository.findByTicketCodeWithDetails(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketCode));
        return ticketMapper.toResponse(ticket);
    }

    @Transactional
    public CheckInResponse checkIn(String ticketCode, CheckInRequest request) {
        var code = checkInCodeRepository.findValidCode(request.checkInCode(), Instant.now())
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

        Instant now = Instant.now();
        int updated = ticketRepository.markAsUsed(ticketCode, now);
        if (updated == 0) {
            return new CheckInResponse(
                    CheckInResult.ALREADY_USED,
                    holderName(ticket),
                    ticket.getTicketType().getName(),
                    null
            );
        }

        return new CheckInResponse(
                CheckInResult.VALID,
                holderName(ticket),
                ticket.getTicketType().getName(),
                now
        );
    }

    private String holderName(Ticket ticket) {
        var r = ticket.getReservation();
        return r.getFirstName() + " " + r.getLastName();
    }

    private void sendConfirmationEmail(EventTicketReservation reservation) {
        notificationPublisher.publish(new NotificationMessage(
                NotificationType.TICKET_CONFIRMATION,
                reservation.getEmail(),
                Map.of(
                        "firstName", reservation.getFirstName(),
                        "ticketsUrl", "/tickets/" + reservation.getExternalId()
                )
        ));
    }
}
