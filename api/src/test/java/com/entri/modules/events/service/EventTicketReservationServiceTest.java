package com.entri.modules.events.service;

import com.entri.common.exception.EventNotOnSaleException;
import com.entri.common.exception.InsufficientTicketsException;
import com.entri.common.exception.MaxTicketsPerOrderExceededException;
import com.entri.common.exception.ResourceNotFoundException;
import com.entri.common.exception.TicketTypeNotAvailableException;
import com.entri.common.exception.TicketTypeNotForEventException;
import com.entri.modules.events.dto.EventTicketReservationItemRequest;
import com.entri.modules.events.dto.EventTicketReservationRequest;
import com.entri.modules.events.entity.Event;
import com.entri.modules.events.entity.EventStatus;
import com.entri.modules.events.entity.TicketType;
import com.entri.modules.events.entity.TicketTypeStatus;
import com.entri.modules.events.repository.EventRepository;
import com.entri.modules.events.repository.EventTicketReservationRepository;
import com.entri.modules.events.repository.TicketTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventTicketReservationServiceTest {

    @Mock Clock clock;
    @Mock EventRepository eventRepository;
    @Mock TicketTypeRepository ticketTypeRepository;
    @Mock EventTicketReservationRepository eventTicketReservationRepository;

    @InjectMocks EventTicketReservationService eventTicketReservationService;

    private static final String EVENT_EXTERNAL_ID = "evt-abc-123";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eventTicketReservationService, "holdDuration", Duration.ofMinutes(10));
        Mockito.lenient().when(clock.instant()).thenReturn(Instant.parse("2026-08-09T10:00:00Z"));
    }

    private Event buildEvent(Long id) {
        return Event.builder()
                .id(id)
                .externalId(EVENT_EXTERNAL_ID)
                .status(EventStatus.PUBLISHED)
                .build();
    }

    private TicketType buildTicketType(Long id, Event event, int quantity, int soldQuantity, BigDecimal price) {
        return TicketType.builder()
                .id(id)
                .event(event)
                .quantity(quantity)
                .soldQuantity(soldQuantity)
                .price(price)
                .name("General Admission")
                .build();
    }

    private TicketType buildTicketTypeWithLimit(Long id, Event event, int quantity, int soldQuantity, BigDecimal price, int maxPerOrder) {
        return TicketType.builder()
                .id(id)
                .event(event)
                .quantity(quantity)
                .soldQuantity(soldQuantity)
                .price(price)
                .name("General Admission")
                .maxTicketsPerOrder(maxPerOrder)
                .build();
    }

    @Test
    void reserveEventTickets_eventNotFound_throwsResourceNotFoundException() {
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.empty());

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 2)
        ));

        assertThatThrownBy(() -> eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(EVENT_EXTERNAL_ID);
    }

    @Test
    void reserveEventTickets_ticketTypeNotFound_throwsResourceNotFoundException() {
        var event = buildEvent(1L);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(99L))).thenReturn(List.of());

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(99L, 1)
        ));

        assertThatThrownBy(() -> eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void reserveEventTickets_ticketTypeBelongsToDifferentEvent_throwsTicketTypeNotForEventException() {
        var event = buildEvent(1L);
        var otherEvent = buildEvent(2L);
        var ticketType = buildTicketType(1L, otherEvent, 100, 0, BigDecimal.valueOf(50));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 1)
        ));

        assertThatThrownBy(() -> eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(TicketTypeNotForEventException.class);
    }

    @Test
    void reserveEventTickets_soldOutTickets_throwsInsufficientTicketsException() {
        var event = buildEvent(1L);
        // 10 total, 8 sold → only 2 available
        var ticketType = buildTicketType(1L, event, 10, 8, BigDecimal.valueOf(50));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 3)
        ));

        assertThatThrownBy(() -> eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(InsufficientTicketsException.class)
                .hasMessageContaining("Only 2 ticket(s) available")
                .hasMessageContaining("requested 3");
    }

    @Test
    void reserveEventTickets_activeReservationsReduceAvailability_throwsInsufficientTicketsException() {
        var event = buildEvent(1L);
        // 10 total, 0 sold, 8 reserved → only 2 available
        var ticketType = TicketType.builder()
                .id(1L)
                .event(event)
                .quantity(10)
                .soldQuantity(0)
                .reservedQuantity(8)
                .price(BigDecimal.valueOf(50))
                .name("General Admission")
                .build();

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 3)
        ));

        assertThatThrownBy(() -> eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(InsufficientTicketsException.class);
    }

    @Test
    void reserveEventTickets_success_savesReservationWithCorrectTotalAmount() {
        var event = buildEvent(1L);
        var ticketType = buildTicketType(1L, event, 10, 0, BigDecimal.valueOf(100));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 3)
        ));

        var response = eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request);

        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(response.expiresAt()).isNotNull();
        assertThat(response.reservationId()).isNotNull();
        verify(eventTicketReservationRepository).save(any());
    }

    @Test
    void reserveEventTickets_usesPessimisticLock_callsFindAllForUpdate() {
        var event = buildEvent(1L);
        var ticketType = buildTicketType(1L, event, 10, 0, BigDecimal.valueOf(100));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));

        eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, new EventTicketReservationRequest(
                List.of(new EventTicketReservationItemRequest(1L, 1))
        ));

        verify(ticketTypeRepository).findAllForUpdate(List.of(1L));
    }

    @Test
    void reserveEventTickets_requestingExactlyAvailableQuantity_succeeds() {
        var event = buildEvent(1L);
        // 10 total, 3 sold, 5 reserved → exactly 2 left
        var ticketType = TicketType.builder()
                .id(1L)
                .event(event)
                .quantity(10)
                .soldQuantity(3)
                .reservedQuantity(5)
                .price(BigDecimal.valueOf(100))
                .name("General Admission")
                .build();

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 2)
        ));

        var response = eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request);

        assertThat(response).isNotNull();
    }

    @Test
    void reserveEventTickets_expiresAt_is10MinutesFromNow() {
        var now = Instant.parse("2026-08-09T10:00:00Z");
        when(clock.instant()).thenReturn(now);

        var event = buildEvent(1L);
        var ticketType = buildTicketType(1L, event, 10, 0, BigDecimal.valueOf(100));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));

        var response = eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, new EventTicketReservationRequest(
                List.of(new EventTicketReservationItemRequest(1L, 1))
        ));

        assertThat(response.expiresAt()).isEqualTo(Instant.parse("2026-08-09T10:10:00Z"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void reserveEventTickets_success_savedReservationHasCorrectItems() {
        var event = buildEvent(1L);
        var ticketType = buildTicketType(1L, event, 10, 0, BigDecimal.valueOf(75));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));

        eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, new EventTicketReservationRequest(
                List.of(new EventTicketReservationItemRequest(1L, 4))
        ));

        var captor = ArgumentCaptor.forClass(com.entri.modules.events.entity.EventTicketReservation.class);
        verify(eventTicketReservationRepository).save(captor.capture());

        var savedReservation = captor.getValue();
        assertThat(savedReservation.getItems()).hasSize(1);
        var item = savedReservation.getItems().getFirst();
        assertThat(item.getTicketType()).isEqualTo(ticketType);
        assertThat(item.getQuantity()).isEqualTo(4);
        assertThat(item.getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(75));
    }

    @Test
    void reserveEventTickets_multipleTicketTypes_totalAmountIsSumOfAllItems() {
        var event = buildEvent(1L);
        var ticketTypeA = buildTicketType(1L, event, 10, 0, BigDecimal.valueOf(100));
        var ticketTypeB = buildTicketType(2L, event, 10, 0, BigDecimal.valueOf(50));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L, 2L))).thenReturn(List.of(ticketTypeA, ticketTypeB));

        // 2 × 100 + 3 × 50 = 350
        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 2),
                new EventTicketReservationItemRequest(2L, 3)
        ));

        var response = eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request);

        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(350));
    }

    @Test
    void reserveEventTickets_ticketTypeWithNoActiveReservations_fullRemainingQuantityIsAvailable() {
        var event = buildEvent(1L);
        // 5 total, 2 sold, 0 reserved → 3 available
        var ticketType = buildTicketType(1L, event, 5, 2, BigDecimal.valueOf(100));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 3)
        ));

        var response = eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request);

        assertThat(response).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void reserveEventTickets_multipleTicketTypes_passesIdsSortedToPreventDeadlocks() {
        var event = buildEvent(1L);
        var ticketType1 = buildTicketType(1L, event, 10, 0, BigDecimal.valueOf(100));
        var ticketType2 = buildTicketType(2L, event, 5, 0, BigDecimal.valueOf(50));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L, 2L))).thenReturn(List.of(ticketType1, ticketType2));

        // Intentionally pass IDs in reverse order to verify service sorts them
        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(2L, 1),
                new EventTicketReservationItemRequest(1L, 1)
        ));

        eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request);

        var captor = ArgumentCaptor.forClass(List.class);
        verify(ticketTypeRepository).findAllForUpdate(captor.capture());
        assertThat(captor.getValue()).containsExactly(1L, 2L);
    }

    @Test
    void reserveEventTickets_quantityExceedsMaxPerOrder_throwsMaxTicketsPerOrderExceededException() {
        var event = buildEvent(1L);
        var ticketType = buildTicketTypeWithLimit(1L, event, 100, 0, BigDecimal.valueOf(50), 2);

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 3)
        ));

        assertThatThrownBy(() -> eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(MaxTicketsPerOrderExceededException.class);
    }

    @Test
    void reserveEventTickets_quantityEqualsMaxPerOrder_succeeds() {
        var event = buildEvent(1L);
        var ticketType = buildTicketTypeWithLimit(1L, event, 100, 0, BigDecimal.valueOf(50), 3);

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 3)
        ));

        assertThat(eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request)).isNotNull();
    }

    @Test
    void reserveEventTickets_eventNotPublished_throwsEventNotOnSaleException() {
        var event = Event.builder()
                .id(1L)
                .externalId(EVENT_EXTERNAL_ID)
                .status(EventStatus.DRAFT)
                .build();

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 1)
        ));

        assertThatThrownBy(() -> eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(EventNotOnSaleException.class);
    }

    @Test
    void reserveEventTickets_inactiveTicketType_throwsTicketTypeNotAvailableException() {
        var event = buildEvent(1L);
        var ticketType = TicketType.builder()
                .id(1L)
                .event(event)
                .quantity(100)
                .soldQuantity(0)
                .price(BigDecimal.valueOf(50))
                .name("General Admission")
                .status(TicketTypeStatus.INACTIVE)
                .build();

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 1)
        ));

        assertThatThrownBy(() -> eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(TicketTypeNotAvailableException.class);
    }

    @Test
    void reserveEventTickets_nullMaxPerOrder_anyQuantityAllowed() {
        var event = buildEvent(1L);
        var ticketType = buildTicketType(1L, event, 100, 0, BigDecimal.valueOf(50));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 50)
        ));

        assertThat(eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request)).isNotNull();
    }

    @Test
    void reserveEventTickets_cancelledEvent_throwsEventNotOnSaleException() {
        var event = Event.builder()
                .id(1L)
                .externalId(EVENT_EXTERNAL_ID)
                .status(EventStatus.CANCELLED)
                .build();

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 1)
        ));

        assertThatThrownBy(() -> eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(EventNotOnSaleException.class);
    }

    @Test
    void reserveEventTickets_completedEvent_throwsEventNotOnSaleException() {
        var event = Event.builder()
                .id(1L)
                .externalId(EVENT_EXTERNAL_ID)
                .status(EventStatus.COMPLETED)
                .build();

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 1)
        ));

        assertThatThrownBy(() -> eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(EventNotOnSaleException.class);
    }

    @Test
    void reserveEventTickets_soldOutTicketType_throwsTicketTypeNotAvailableException() {
        var event = buildEvent(1L);
        var ticketType = TicketType.builder()
                .id(1L)
                .event(event)
                .quantity(100)
                .soldQuantity(100)
                .price(BigDecimal.valueOf(50))
                .name("General Admission")
                .status(TicketTypeStatus.SOLD_OUT)
                .build();

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 1)
        ));

        assertThatThrownBy(() -> eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(TicketTypeNotAvailableException.class);
    }

    @Test
    void reserveEventTickets_oneOfMultipleTicketTypesInactive_throwsTicketTypeNotAvailableException() {
        var event = buildEvent(1L);
        var activeTicketType = buildTicketType(1L, event, 100, 0, BigDecimal.valueOf(50));
        var inactiveTicketType = TicketType.builder()
                .id(2L)
                .event(event)
                .quantity(100)
                .soldQuantity(0)
                .price(BigDecimal.valueOf(50))
                .name("VIP")
                .status(TicketTypeStatus.INACTIVE)
                .build();

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L, 2L))).thenReturn(List.of(activeTicketType, inactiveTicketType));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 1),
                new EventTicketReservationItemRequest(2L, 1)
        ));

        assertThatThrownBy(() -> eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(TicketTypeNotAvailableException.class);
    }

    @Test
    void reserveEventTickets_oneOfMultipleTicketTypesExceedsMaxPerOrder_throwsMaxTicketsPerOrderExceededException() {
        var event = buildEvent(1L);
        var ticketTypeA = buildTicketTypeWithLimit(1L, event, 100, 0, BigDecimal.valueOf(100), 5);
        var ticketTypeB = buildTicketTypeWithLimit(2L, event, 100, 0, BigDecimal.valueOf(50), 2);

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L, 2L))).thenReturn(List.of(ticketTypeA, ticketTypeB));

        // ticketTypeA is within limit, ticketTypeB exceeds it
        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 3),
                new EventTicketReservationItemRequest(2L, 3)
        ));

        assertThatThrownBy(() -> eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(MaxTicketsPerOrderExceededException.class);
    }

    @Test
    void reserveEventTickets_duplicateTicketTypeIds_throwsIllegalArgumentException() {
        var event = buildEvent(1L);
        var ticketType = buildTicketType(1L, event, 10, 0, BigDecimal.valueOf(50));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L, 1L))).thenReturn(List.of(ticketType));

        // Same ticket type ID twice — each qty passes individually but combined would oversell
        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 3),
                new EventTicketReservationItemRequest(1L, 3)
        ));

        assertThatThrownBy(() -> eventTicketReservationService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate ticket type IDs");
    }
}
