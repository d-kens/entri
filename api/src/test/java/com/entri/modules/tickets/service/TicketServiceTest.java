package com.entri.modules.tickets.service;

import com.entri.events.entity.Event;
import com.entri.events.entity.EventTicketReservation;
import com.entri.events.entity.EventTicketReservationItem;
import com.entri.events.entity.TicketType;
import com.entri.events.repository.EventRepository;
import com.entri.events.repository.EventTicketReservationRepository;
import com.entri.exception.BadRequestException;
import com.entri.exception.ForbiddenException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.notification.NotificationEventPublisher;
import com.entri.notification.dto.NotificationEvent;
import com.entri.security.UserPrincipal;
import com.entri.tickets.dto.CheckInRequest;
import com.entri.tickets.dto.CheckInResult;
import com.entri.tickets.dto.TicketFilter;
import com.entri.tickets.dto.TicketResponse;
import com.entri.tickets.entity.EventCheckInCode;
import com.entri.tickets.entity.Ticket;
import com.entri.tickets.entity.TicketStatus;
import com.entri.tickets.mapper.TicketMapper;
import com.entri.tickets.repository.EventCheckInCodeRepository;
import com.entri.tickets.repository.TicketRepository;
import com.entri.tickets.service.TicketService;
import com.entri.users.entity.Role;
import com.entri.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    private static final String EVENT_EXTERNAL_ID = "evt-abc-123";

    @Mock TicketRepository ticketRepository;
    @Mock EventTicketReservationRepository reservationRepository;
    @Mock EventCheckInCodeRepository checkInCodeRepository;
    @Mock EventRepository eventRepository;
    @Mock TicketMapper ticketMapper;
    @Mock NotificationEventPublisher notificationEventPublisher;

    @InjectMocks TicketService ticketService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ticketService, "appBaseUrl", "https://entri.example.com");
    }

    private UserPrincipal organizerPrincipal(String externalKey) {
        return new UserPrincipal(User.builder().externalKey(externalKey).role(Role.ORGANIZER).build());
    }

    private UserPrincipal adminPrincipal() {
        return new UserPrincipal(User.builder().externalKey("admin-key").role(Role.ADMIN).build());
    }

    private Event buildEvent(String organizerKey) {
        return Event.builder()
                .id(1L)
                .externalId(EVENT_EXTERNAL_ID)
                .title("Some Event")
                .organizer(User.builder().externalKey(organizerKey).role(Role.ORGANIZER).build())
                .build();
    }

    @Test
    void getTickets_eventNotFound_throwsResourceNotFoundException() {
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTickets(EVENT_EXTERNAL_ID, new TicketFilter(0, 10), adminPrincipal()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getTickets_requestingUserNotOwnerOrAdmin_throwsForbiddenException() {
        var event = buildEvent("owner-key");
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> ticketService.getTickets(EVENT_EXTERNAL_ID, new TicketFilter(0, 10), organizerPrincipal("someone-else")))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getTickets_ownerRequesting_returnsPaginatedResponse() {
        var event = buildEvent("owner-key");
        var ticket = Ticket.builder().id(1L).build();
        var page = new PageImpl<>(List.of(ticket), PageRequest.of(0, 10), 1);
        var ticketResponse = mockTicketResponse();

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketRepository.findByEventExternalId(eq(EVENT_EXTERNAL_ID), any())).thenReturn(page);
        when(ticketMapper.toResponse(ticket)).thenReturn(ticketResponse);

        var result = ticketService.getTickets(EVENT_EXTERNAL_ID, new TicketFilter(0, 10), organizerPrincipal("owner-key"));

        assertThat(result.content()).containsExactly(ticketResponse);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void getCheckInStats_eventNotFound_throwsResourceNotFoundException() {
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getCheckInStats(EVENT_EXTERNAL_ID, adminPrincipal()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCheckInStats_notOwnerOrAdmin_throwsForbiddenException() {
        var event = buildEvent("owner-key");
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> ticketService.getCheckInStats(EVENT_EXTERNAL_ID, organizerPrincipal("someone-else")))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getCheckInStats_noTicketsSold_returnsZeroPercentage() {
        var event = buildEvent("owner-key");
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketRepository.countByEventExternalId(EVENT_EXTERNAL_ID)).thenReturn(0L);
        when(ticketRepository.countCheckedInByEventExternalId(EVENT_EXTERNAL_ID)).thenReturn(0L);
        when(ticketRepository.findRecentCheckIns(eq(EVENT_EXTERNAL_ID), any())).thenReturn(List.of());

        var result = ticketService.getCheckInStats(EVENT_EXTERNAL_ID, adminPrincipal());

        assertThat(result.checkInPercentage()).isEqualTo(0.0);
        assertThat(result.totalTickets()).isEqualTo(0);
    }

    @Test
    void getCheckInStats_someTicketsCheckedIn_computesPercentage() {
        var event = buildEvent("owner-key");
        var ticketType = TicketType.builder().name("VIP").build();
        var checkedInTicket = Ticket.builder()
                .ticketCode("ABC123")
                .ticketType(ticketType)
                .checkedInAt(Instant.now())
                .build();

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketRepository.countByEventExternalId(EVENT_EXTERNAL_ID)).thenReturn(4L);
        when(ticketRepository.countCheckedInByEventExternalId(EVENT_EXTERNAL_ID)).thenReturn(1L);
        when(ticketRepository.findRecentCheckIns(eq(EVENT_EXTERNAL_ID), any())).thenReturn(List.of(checkedInTicket));

        var result = ticketService.getCheckInStats(EVENT_EXTERNAL_ID, adminPrincipal());

        assertThat(result.checkInPercentage()).isEqualTo(25.0);
        assertThat(result.recentCheckIns()).hasSize(1);
        assertThat(result.recentCheckIns().getFirst().ticketCode()).isEqualTo("ABC123");
    }

    @Test
    void generateTickets_reservationNotFound_throwsResourceNotFoundException() {
        when(reservationRepository.findByExternalIdWithItems("res-123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.generateTickets("res-123"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void generateTickets_ticketsAlreadyExist_doesNothing() {
        var reservation = EventTicketReservation.builder().id(1L).build();
        when(reservationRepository.findByExternalIdWithItems("res-123")).thenReturn(Optional.of(reservation));
        when(ticketRepository.existsByReservationId(1L)).thenReturn(true);

        ticketService.generateTickets("res-123");

        verify(ticketRepository, never()).saveAll(any());
        verify(notificationEventPublisher, never()).publish(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateTickets_success_createsOneTicketPerQuantityAndSendsNotification() {
        var event = Event.builder().id(1L).title("Some Event").build();
        var ticketType = TicketType.builder().id(1L).name("General").build();
        var item = EventTicketReservationItem.builder().ticketType(ticketType).quantity(3).build();
        var reservation = EventTicketReservation.builder()
                .id(1L)
                .externalId("res-123")
                .event(event)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .phoneNumber("0712345678")
                .items(List.of(item))
                .build();

        when(reservationRepository.findByExternalIdWithItems("res-123")).thenReturn(Optional.of(reservation));
        when(ticketRepository.existsByReservationId(1L)).thenReturn(false);

        ticketService.generateTickets("res-123");

        var captor = ArgumentCaptor.forClass(List.class);
        verify(ticketRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(3);

        var notificationCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationEventPublisher).publish(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().recipient().email()).isEqualTo("jane@example.com");
        assertThat(notificationCaptor.getValue().payload().get("ticketsUrl")).isEqualTo("https://entri.example.com/tickets/res-123");
    }

    @Test
    void getTicketsByReservation_returnsMappedTickets() {
        var ticket = Ticket.builder().id(1L).build();
        var response = mockTicketResponse();
        when(ticketRepository.findByReservationExternalId("res-123")).thenReturn(List.of(ticket));
        when(ticketMapper.toResponse(ticket)).thenReturn(response);

        var result = ticketService.getTicketsByReservation("res-123");

        assertThat(result).containsExactly(response);
    }

    @Test
    void getTicket_notFound_throwsResourceNotFoundException() {
        when(ticketRepository.findByExternalIdWithDetails("ext-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicket("ext-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getTicket_found_returnsMappedResponse() {
        var ticket = Ticket.builder().id(1L).build();
        var response = mockTicketResponse();
        when(ticketRepository.findByExternalIdWithDetails("ext-1")).thenReturn(Optional.of(ticket));
        when(ticketMapper.toResponse(ticket)).thenReturn(response);

        var result = ticketService.getTicket("ext-1");

        assertThat(result).isEqualTo(response);
    }

    @Test
    void checkIn_invalidOrExpiredCode_throwsBadRequestException() {
        when(checkInCodeRepository.findValidCode(anyString(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.checkIn("TICKET1", new CheckInRequest("badcode")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void checkIn_ticketNotFound_returnsInvalidResult() {
        var event = Event.builder().id(1L).build();
        var code = EventCheckInCode.builder().code("VALIDCODE").event(event).build();
        when(checkInCodeRepository.findValidCode(any(), any())).thenReturn(Optional.of(code));
        when(ticketRepository.findByTicketCodeWithDetails("TICKET1")).thenReturn(Optional.empty());

        var result = ticketService.checkIn("TICKET1", new CheckInRequest("validcode"));

        assertThat(result.result()).isEqualTo(CheckInResult.INVALID);
    }

    @Test
    void checkIn_ticketBelongsToDifferentEvent_returnsInvalidResult() {
        var codeEvent = Event.builder().id(1L).build();
        var ticketEvent = Event.builder().id(2L).build();
        var code = EventCheckInCode.builder().code("VALIDCODE").event(codeEvent).build();
        var reservation = EventTicketReservation.builder().firstName("Jane").lastName("Doe").build();
        var ticket = Ticket.builder()
                .event(ticketEvent)
                .reservation(reservation)
                .status(TicketStatus.VALID)
                .build();

        when(checkInCodeRepository.findValidCode(any(), any())).thenReturn(Optional.of(code));
        when(ticketRepository.findByTicketCodeWithDetails("TICKET1")).thenReturn(Optional.of(ticket));

        var result = ticketService.checkIn("TICKET1", new CheckInRequest("validcode"));

        assertThat(result.result()).isEqualTo(CheckInResult.INVALID);
    }

    @Test
    void checkIn_ticketAlreadyUsed_returnsAlreadyUsedResult() {
        var event = Event.builder().id(1L).build();
        var code = EventCheckInCode.builder().code("VALIDCODE").event(event).build();
        var reservation = EventTicketReservation.builder().firstName("Jane").lastName("Doe").build();
        var ticketType = TicketType.builder().name("VIP").build();
        var checkedInAt = Instant.now();
        var ticket = Ticket.builder()
                .event(event)
                .reservation(reservation)
                .ticketType(ticketType)
                .status(TicketStatus.USED)
                .checkedInAt(checkedInAt)
                .build();

        when(checkInCodeRepository.findValidCode(any(), any())).thenReturn(Optional.of(code));
        when(ticketRepository.findByTicketCodeWithDetails("TICKET1")).thenReturn(Optional.of(ticket));

        var result = ticketService.checkIn("TICKET1", new CheckInRequest("validcode"));

        assertThat(result.result()).isEqualTo(CheckInResult.ALREADY_USED);
        assertThat(result.holderName()).isEqualTo("Jane Doe");
        assertThat(result.checkedInAt()).isEqualTo(checkedInAt);
        verify(ticketRepository, never()).markAsUsed(any(), any());
    }

    @Test
    void checkIn_validTicket_marksAsUsedAndReturnsValidResult() {
        var event = Event.builder().id(1L).build();
        var code = EventCheckInCode.builder().code("VALIDCODE").event(event).build();
        var reservation = EventTicketReservation.builder().firstName("Jane").lastName("Doe").build();
        var ticketType = TicketType.builder().name("VIP").build();
        var ticket = Ticket.builder()
                .ticketCode("TICKET1")
                .event(event)
                .reservation(reservation)
                .ticketType(ticketType)
                .status(TicketStatus.VALID)
                .build();

        when(checkInCodeRepository.findValidCode(any(), any())).thenReturn(Optional.of(code));
        when(ticketRepository.findByTicketCodeWithDetails("TICKET1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.markAsUsed(eq("TICKET1"), any())).thenReturn(1);

        var result = ticketService.checkIn("TICKET1", new CheckInRequest("validcode"));

        assertThat(result.result()).isEqualTo(CheckInResult.VALID);
        assertThat(result.holderName()).isEqualTo("Jane Doe");
        assertThat(result.ticketType()).isEqualTo("VIP");
    }

    @Test
    void checkIn_concurrentCheckInLosesRace_returnsAlreadyUsedResult() {
        var event = Event.builder().id(1L).build();
        var code = EventCheckInCode.builder().code("VALIDCODE").event(event).build();
        var reservation = EventTicketReservation.builder().firstName("Jane").lastName("Doe").build();
        var ticketType = TicketType.builder().name("VIP").build();
        var initialTicket = Ticket.builder()
                .ticketCode("TICKET1")
                .event(event)
                .reservation(reservation)
                .ticketType(ticketType)
                .status(TicketStatus.VALID)
                .build();
        var checkedInAt = Instant.now();
        var refreshedTicket = Ticket.builder()
                .ticketCode("TICKET1")
                .event(event)
                .reservation(reservation)
                .ticketType(ticketType)
                .status(TicketStatus.USED)
                .checkedInAt(checkedInAt)
                .build();

        when(checkInCodeRepository.findValidCode(any(), any())).thenReturn(Optional.of(code));
        when(ticketRepository.findByTicketCodeWithDetails("TICKET1"))
                .thenReturn(Optional.of(initialTicket))
                .thenReturn(Optional.of(refreshedTicket));
        when(ticketRepository.markAsUsed(eq("TICKET1"), any())).thenReturn(0);

        var result = ticketService.checkIn("TICKET1", new CheckInRequest("validcode"));

        assertThat(result.result()).isEqualTo(CheckInResult.ALREADY_USED);
        assertThat(result.checkedInAt()).isEqualTo(checkedInAt);
        verify(ticketRepository, times(2)).findByTicketCodeWithDetails("TICKET1");
    }

    private TicketResponse mockTicketResponse() {
        return new TicketResponse(
                "ticket-ext-1", "TICKET1", "res-ext-1", EVENT_EXTERNAL_ID, "Some Event",
                Instant.now(), Instant.now(), "Venue", "City", "KES",
                "General", java.math.BigDecimal.TEN, "Jane", "Doe", "VALID", null
        );
    }
}
