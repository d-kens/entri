package com.entri.modules.events.service;

import com.entri.common.exception.InsufficientTicketsException;
import com.entri.common.exception.ResourceNotFoundException;
import com.entri.modules.events.dto.EventTicketReservationItemRequest;
import com.entri.modules.events.dto.EventTicketReservationRequest;
import com.entri.modules.events.entity.Event;
import com.entri.modules.events.entity.TicketReservationStatus;
import com.entri.modules.events.entity.TicketType;
import com.entri.modules.events.repository.EventRepository;
import com.entri.modules.events.repository.EventTicketReservationRepository;
import com.entri.modules.events.repository.TicketTypeRepository;
import com.entri.modules.events.repository.TicketTypeReservationSum;
import com.entri.modules.events.service.mapper.TicketTypeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketTypeServiceReserveTicketsTest {

    @Mock EventRepository eventRepository;
    @Mock TicketTypeMapper ticketTypeMapper;
    @Mock TicketTypeRepository ticketTypeRepository;
    @Mock EventTicketReservationRepository eventTicketReservationRepository;

    @InjectMocks TicketTypeService ticketTypeService;

    private static final String EVENT_EXTERNAL_ID = "evt-abc-123";

    private Event buildEvent(Long id) {
        return Event.builder()
                .id(id)
                .externalId(EVENT_EXTERNAL_ID)
                .build();
    }

    private TicketType buildTicketType(Long id, Event event, int quantity, int soldQuantity, BigDecimal price) {
        return TicketType.builder()
                .id(id)
                .event(event)
                .quantity(quantity)
                .soldQuantity(soldQuantity)
                .price(price)
                .currency("KES")
                .name("General Admission")
                .build();
    }

    @Test
    void reserveEventTickets_eventNotFound_throwsResourceNotFoundException() {
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.empty());

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 2)
        ));

        assertThatThrownBy(() -> ticketTypeService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
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

        assertThatThrownBy(() -> ticketTypeService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void reserveEventTickets_ticketTypeBelongsToDifferentEvent_throwsResourceNotFoundException() {
        var event = buildEvent(1L);
        var otherEvent = buildEvent(2L);
        var ticketType = buildTicketType(1L, otherEvent, 100, 0, BigDecimal.valueOf(50));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 1)
        ));

        assertThatThrownBy(() -> ticketTypeService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(EVENT_EXTERNAL_ID);
    }

    @Test
    void reserveEventTickets_soldOutTickets_throwsInsufficientTicketsException() {
        var event = buildEvent(1L);
        // 10 total, 8 sold → only 2 available
        var ticketType = buildTicketType(1L, event, 10, 8, BigDecimal.valueOf(50));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));
        when(eventTicketReservationRepository.sumActiveReservationsByTicketTypes(
                anyCollection(), eq(TicketReservationStatus.PENDING)))
                .thenReturn(List.of());

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 3)
        ));

        assertThatThrownBy(() -> ticketTypeService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(InsufficientTicketsException.class)
                .hasMessageContaining("1");
    }

    @Test
    void reserveEventTickets_activeReservationsReduceAvailability_throwsInsufficientTicketsException() {
        var event = buildEvent(1L);
        // 10 total, 0 sold, but 8 already reserved by others
        var ticketType = buildTicketType(1L, event, 10, 0, BigDecimal.valueOf(50));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));
        when(eventTicketReservationRepository.sumActiveReservationsByTicketTypes(
                anyCollection(), eq(TicketReservationStatus.PENDING)))
                .thenReturn(List.of(new TicketTypeReservationSum(1L, 8L)));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 3)
        ));

        assertThatThrownBy(() -> ticketTypeService.reserveEventTickets(EVENT_EXTERNAL_ID, request))
                .isInstanceOf(InsufficientTicketsException.class);
    }

    @Test
    void reserveEventTickets_success_savesReservationWithCorrectTotalAmount() {
        var event = buildEvent(1L);
        var ticketType = buildTicketType(1L, event, 10, 0, BigDecimal.valueOf(100));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));
        when(eventTicketReservationRepository.sumActiveReservationsByTicketTypes(
                anyCollection(), eq(TicketReservationStatus.PENDING)))
                .thenReturn(List.of());

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 3)
        ));

        var response = ticketTypeService.reserveEventTickets(EVENT_EXTERNAL_ID, request);

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
        when(eventTicketReservationRepository.sumActiveReservationsByTicketTypes(
                anyCollection(), eq(TicketReservationStatus.PENDING)))
                .thenReturn(List.of());

        ticketTypeService.reserveEventTickets(EVENT_EXTERNAL_ID, new EventTicketReservationRequest(
                List.of(new EventTicketReservationItemRequest(1L, 1))
        ));

        verify(ticketTypeRepository).findAllForUpdate(List.of(1L));
    }

    @Test
    void reserveEventTickets_requestingExactlyAvailableQuantity_succeeds() {
        var event = buildEvent(1L);
        // 10 total, 3 sold, 5 reserved → exactly 2 left
        var ticketType = buildTicketType(1L, event, 10, 3, BigDecimal.valueOf(100));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));
        when(eventTicketReservationRepository.sumActiveReservationsByTicketTypes(
                anyCollection(), eq(TicketReservationStatus.PENDING)))
                .thenReturn(List.of(new TicketTypeReservationSum(1L, 5L)));

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 2)
        ));

        var response = ticketTypeService.reserveEventTickets(EVENT_EXTERNAL_ID, request);

        assertThat(response).isNotNull();
    }

    @Test
    void reserveEventTickets_expiresAt_isApproximately10MinutesFromNow() {
        var event = buildEvent(1L);
        var ticketType = buildTicketType(1L, event, 10, 0, BigDecimal.valueOf(100));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));
        when(eventTicketReservationRepository.sumActiveReservationsByTicketTypes(
                anyCollection(), eq(TicketReservationStatus.PENDING)))
                .thenReturn(List.of());

        var before = Instant.now();
        var response = ticketTypeService.reserveEventTickets(EVENT_EXTERNAL_ID, new EventTicketReservationRequest(
                List.of(new EventTicketReservationItemRequest(1L, 1))
        ));

        assertThat(response.expiresAt())
                .isCloseTo(before.plus(10, ChronoUnit.MINUTES), within(5, ChronoUnit.SECONDS));
    }

    @Test
    @SuppressWarnings("unchecked")
    void reserveEventTickets_success_savedReservationHasCorrectItems() {
        var event = buildEvent(1L);
        var ticketType = buildTicketType(1L, event, 10, 0, BigDecimal.valueOf(75));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));
        when(eventTicketReservationRepository.sumActiveReservationsByTicketTypes(
                anyCollection(), eq(TicketReservationStatus.PENDING)))
                .thenReturn(List.of());

        ticketTypeService.reserveEventTickets(EVENT_EXTERNAL_ID, new EventTicketReservationRequest(
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
        when(eventTicketReservationRepository.sumActiveReservationsByTicketTypes(
                anyCollection(), eq(TicketReservationStatus.PENDING)))
                .thenReturn(List.of());

        // 2 × 100 + 3 × 50 = 350
        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 2),
                new EventTicketReservationItemRequest(2L, 3)
        ));

        var response = ticketTypeService.reserveEventTickets(EVENT_EXTERNAL_ID, request);

        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(350));
    }

    @Test
    void reserveEventTickets_ticketTypeWithNoActiveReservations_fullRemainingQuantityIsAvailable() {
        var event = buildEvent(1L);
        // 5 total, 2 sold → 3 available; no active reservations in DB result
        var ticketType = buildTicketType(1L, event, 5, 2, BigDecimal.valueOf(100));

        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findAllForUpdate(List.of(1L))).thenReturn(List.of(ticketType));
        when(eventTicketReservationRepository.sumActiveReservationsByTicketTypes(
                anyCollection(), eq(TicketReservationStatus.PENDING)))
                .thenReturn(List.of()); // no entry for this ticket type → defaults to 0

        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(1L, 3)
        ));

        var response = ticketTypeService.reserveEventTickets(EVENT_EXTERNAL_ID, request);

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
        when(eventTicketReservationRepository.sumActiveReservationsByTicketTypes(
                anyCollection(), eq(TicketReservationStatus.PENDING)))
                .thenReturn(List.of());

        // Intentionally pass IDs in reverse order to verify service sorts them
        var request = new EventTicketReservationRequest(List.of(
                new EventTicketReservationItemRequest(2L, 1),
                new EventTicketReservationItemRequest(1L, 1)
        ));

        ticketTypeService.reserveEventTickets(EVENT_EXTERNAL_ID, request);

        var captor = ArgumentCaptor.forClass(List.class);
        verify(ticketTypeRepository).findAllForUpdate(captor.capture());
        assertThat(captor.getValue()).containsExactly(1L, 2L);
    }
}
