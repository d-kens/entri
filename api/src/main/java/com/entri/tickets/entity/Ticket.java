package com.entri.tickets.entity;

import com.entri.events.entity.Event;
import com.entri.events.entity.EventTicketReservation;
import com.entri.events.entity.TicketType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_external_id", nullable = false, unique = true, length = 36)
    @Builder.Default
    private String ticketExternalId = UUID.randomUUID().toString();

    @Column(name = "ticket_code", nullable = false, unique = true, length = 12)
    @Builder.Default
    private String ticketCode = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private EventTicketReservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TicketStatus status = TicketStatus.VALID;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;
}
