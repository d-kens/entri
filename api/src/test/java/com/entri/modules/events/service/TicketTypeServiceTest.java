package com.entri.modules.events.service;

import com.entri.events.dto.TicketTypeRequest;
import com.entri.events.entity.Event;
import com.entri.events.entity.TicketType;
import com.entri.events.mapper.TicketTypeMapper;
import com.entri.events.repository.EventRepository;
import com.entri.events.repository.TicketTypeRepository;
import com.entri.events.service.TicketTypeService;
import com.entri.exception.BadRequestException;
import com.entri.exception.ForbiddenException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.security.UserPrincipal;
import com.entri.users.entity.Role;
import com.entri.users.entity.User;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketTypeServiceTest {

    @Mock EventRepository eventRepository;
    @Mock TicketTypeMapper ticketTypeMapper;
    @Mock TicketTypeRepository ticketTypeRepository;

    @InjectMocks TicketTypeService ticketTypeService;

    private static final String EVENT_EXTERNAL_ID = "evt-abc-123";

    private User buildUser(String externalKey, Role role) {
        return User.builder().externalKey(externalKey).role(role).build();
    }

    private Event buildEvent(Long id, User organizer) {
        return Event.builder()
                .id(id)
                .externalId(EVENT_EXTERNAL_ID)
                .organizer(organizer)
                .startTime(Instant.now())
                .endTime(Instant.now().plus(2, ChronoUnit.HOURS))
                .build();
    }

    private TicketType buildTicketType(Long id, Event event) {
        return TicketType.builder()
                .id(id)
                .event(event)
                .name("General Admission")
                .price(BigDecimal.valueOf(50))
                .quantity(100)
                .build();
    }

    private TicketTypeRequest buildRequest(Instant saleStart, Instant saleEnd) {
        return new TicketTypeRequest("General Admission", "desc", BigDecimal.valueOf(50), 100, 5, saleStart, saleEnd);
    }

    @Test
    void getTicketType_notFound_throwsResourceNotFoundException() {
        when(ticketTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketTypeService.getTicketType(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getTicketType_found_returnsMappedResponse() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer);
        var ticketType = buildTicketType(1L, event);
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
        var expected = new com.entri.events.dto.TicketTypeResponse(
                1L, "General Admission", "desc", BigDecimal.valueOf(50), 100, 100, 5, null, null, null, null
        );
        when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(expected);

        var result = ticketTypeService.getTicketType(1L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getTicketTypesByEventExternalId_eventNotFound_throwsResourceNotFoundException() {
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketTypeService.getTicketTypesByEventExternalId(EVENT_EXTERNAL_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getTicketTypesByEventExternalId_eventFound_returnsMappedTicketTypes() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer);
        var ticketType = buildTicketType(1L, event);
        event.setTicketTypes(List.of(ticketType));
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var expected = new com.entri.events.dto.TicketTypeResponse(
                1L, "General Admission", "desc", BigDecimal.valueOf(50), 100, 100, 5, null, null, null, null
        );
        when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(expected);

        var result = ticketTypeService.getTicketTypesByEventExternalId(EVENT_EXTERNAL_ID);

        assertThat(result).containsExactly(expected);
    }

    @Test
    void updateTicketType_notFound_throwsResourceNotFoundException() {
        when(ticketTypeRepository.findById(99L)).thenReturn(Optional.empty());
        var principal = new UserPrincipal(buildUser("org-key", Role.ORGANIZER));

        assertThatThrownBy(() -> ticketTypeService.updateTicketType(99L, buildRequest(null, null), principal))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateTicketType_notOwnerAndNotAdmin_throwsForbiddenException() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer);
        var ticketType = buildTicketType(1L, event);
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
        var otherUser = new UserPrincipal(buildUser("other-key", Role.ORGANIZER));

        assertThatThrownBy(() -> ticketTypeService.updateTicketType(1L, buildRequest(null, null), otherUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void updateTicketType_saleStartOnlyProvided_throwsBadRequestException() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer);
        var ticketType = buildTicketType(1L, event);
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
        var principal = new UserPrincipal(organizer);

        var request = buildRequest(Instant.now(), null);

        assertThatThrownBy(() -> ticketTypeService.updateTicketType(1L, request, principal))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must both be provided or both omitted");
    }

    @Test
    void updateTicketType_saleEndBeforeSaleStart_throwsBadRequestException() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer);
        var ticketType = buildTicketType(1L, event);
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
        var principal = new UserPrincipal(organizer);

        var saleStart = event.getStartTime().plus(1, ChronoUnit.HOURS);
        var saleEnd = saleStart.minus(1, ChronoUnit.MINUTES);
        var request = buildRequest(saleStart, saleEnd);

        assertThatThrownBy(() -> ticketTypeService.updateTicketType(1L, request, principal))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Sale end date must be after sale start date");
    }

    @Test
    void updateTicketType_saleStartBeforeEventStart_throwsBadRequestException() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer);
        var ticketType = buildTicketType(1L, event);
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
        var principal = new UserPrincipal(organizer);

        var saleStart = event.getStartTime().minus(1, ChronoUnit.HOURS);
        var saleEnd = event.getEndTime();
        var request = buildRequest(saleStart, saleEnd);

        assertThatThrownBy(() -> ticketTypeService.updateTicketType(1L, request, principal))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must not be before the event start date");
    }

    @Test
    void updateTicketType_saleEndAfterEventEnd_throwsBadRequestException() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer);
        var ticketType = buildTicketType(1L, event);
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
        var principal = new UserPrincipal(organizer);

        var saleStart = event.getStartTime();
        var saleEnd = event.getEndTime().plus(1, ChronoUnit.HOURS);
        var request = buildRequest(saleStart, saleEnd);

        assertThatThrownBy(() -> ticketTypeService.updateTicketType(1L, request, principal))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must not be after the event end date");
    }

    @Test
    void updateTicketType_validRequestByOwner_updatesAndSavesTicketType() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer);
        var ticketType = buildTicketType(1L, event);
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
        var principal = new UserPrincipal(organizer);
        var expected = new com.entri.events.dto.TicketTypeResponse(
                1L, "VIP", "vip desc", BigDecimal.valueOf(200), 50, 50, 2, null, null, null, null
        );
        when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(expected);

        var request = new TicketTypeRequest("VIP", "vip desc", BigDecimal.valueOf(200), 50, 2, null, null);

        var result = ticketTypeService.updateTicketType(1L, request, principal);

        assertThat(result).isEqualTo(expected);
        assertThat(ticketType.getName()).isEqualTo("VIP");
        assertThat(ticketType.getDescription()).isEqualTo("vip desc");
        assertThat(ticketType.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(ticketType.getQuantity()).isEqualTo(50);
        assertThat(ticketType.getMaxTicketsPerOrder()).isEqualTo(2);
        verify(ticketTypeRepository).save(ticketType);
    }

    @Test
    void updateTicketType_validRequestByAdmin_succeeds() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer);
        var ticketType = buildTicketType(1L, event);
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
        var admin = new UserPrincipal(buildUser("admin-key", Role.ADMIN));
        var expected = new com.entri.events.dto.TicketTypeResponse(
                1L, "VIP", "vip desc", BigDecimal.valueOf(200), 50, 50, 2, null, null, null, null
        );
        when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(expected);

        var request = new TicketTypeRequest("VIP", "vip desc", BigDecimal.valueOf(200), 50, 2, null, null);

        assertThat(ticketTypeService.updateTicketType(1L, request, admin)).isEqualTo(expected);
    }

    @Test
    void deleteTicketType_notFound_throwsResourceNotFoundException() {
        when(ticketTypeRepository.findById(99L)).thenReturn(Optional.empty());
        var principal = new UserPrincipal(buildUser("org-key", Role.ORGANIZER));

        assertThatThrownBy(() -> ticketTypeService.deleteTicketType(99L, principal))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteTicketType_notOwnerAndNotAdmin_throwsForbiddenException() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer);
        var ticketType = buildTicketType(1L, event);
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
        var otherUser = new UserPrincipal(buildUser("other-key", Role.ORGANIZER));

        assertThatThrownBy(() -> ticketTypeService.deleteTicketType(1L, otherUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void deleteTicketType_alreadyDeleted_doesNotChangeDeletedAt() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer);
        var ticketType = buildTicketType(1L, event);
        var originalDeletedAt = Instant.now().minus(1, ChronoUnit.DAYS);
        ticketType.setDeletedAt(originalDeletedAt);
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
        var principal = new UserPrincipal(organizer);

        ticketTypeService.deleteTicketType(1L, principal);

        assertThat(ticketType.getDeletedAt()).isEqualTo(originalDeletedAt);
    }

    @Test
    void deleteTicketType_notYetDeleted_setsDeletedAt() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer);
        var ticketType = buildTicketType(1L, event);
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
        var principal = new UserPrincipal(organizer);

        ticketTypeService.deleteTicketType(1L, principal);

        assertThat(ticketType.getDeletedAt()).isNotNull();
    }

    @Test
    void createEventTicketType_eventNotFound_throwsResourceNotFoundException() {
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.empty());
        var principal = new UserPrincipal(buildUser("org-key", Role.ORGANIZER));

        assertThatThrownBy(() -> ticketTypeService.createEventTicketType(EVENT_EXTERNAL_ID, buildRequest(null, null), principal))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createEventTicketType_notOwnerAndNotAdmin_throwsForbiddenException() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var otherUser = new UserPrincipal(buildUser("other-key", Role.ORGANIZER));

        assertThatThrownBy(() -> ticketTypeService.createEventTicketType(EVENT_EXTERNAL_ID, buildRequest(null, null), otherUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void createEventTicketType_validRequestByOwner_savesAndReturnsResponse() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var principal = new UserPrincipal(organizer);
        var expected = new com.entri.events.dto.TicketTypeResponse(
                1L, "General Admission", "desc", BigDecimal.valueOf(50), 100, 100, 5, null, null, null, null
        );
        when(ticketTypeMapper.toTicketTypeResponse(any(TicketType.class))).thenReturn(expected);

        var result = ticketTypeService.createEventTicketType(EVENT_EXTERNAL_ID, buildRequest(null, null), principal);

        assertThat(result).isEqualTo(expected);
        verify(ticketTypeRepository).save(any(TicketType.class));
    }

    @Test
    void createEventTicketType_validRequest_savedEntityHasEventAndRequestFields() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var principal = new UserPrincipal(organizer);

        ticketTypeService.createEventTicketType(EVENT_EXTERNAL_ID, buildRequest(null, null), principal);

        var captor = ArgumentCaptor.forClass(TicketType.class);
        verify(ticketTypeRepository).save(captor.capture());
        assertThat(captor.getValue().getEvent()).isEqualTo(event);
        assertThat(captor.getValue().getName()).isEqualTo("General Admission");
        assertThat(captor.getValue().getQuantity()).isEqualTo(100);
        assertThat(captor.getValue().getMaxTicketsPerOrder()).isEqualTo(5);
    }

    @Test
    void createEventTicketType_saleEndBeforeSaleStart_throwsBadRequestException() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var principal = new UserPrincipal(organizer);

        var saleStart = event.getStartTime().plus(1, ChronoUnit.HOURS);
        var saleEnd = saleStart.minus(1, ChronoUnit.MINUTES);
        var request = buildRequest(saleStart, saleEnd);

        assertThatThrownBy(() -> ticketTypeService.createEventTicketType(EVENT_EXTERNAL_ID, request, principal))
                .isInstanceOf(BadRequestException.class);
    }
}
