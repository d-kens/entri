package com.entri.modules.events.service;

import com.entri.events.dto.EventFilter;
import com.entri.events.dto.EventRequest;
import com.entri.events.dto.EventResponse;
import com.entri.events.entity.Event;
import com.entri.events.entity.EventCategory;
import com.entri.events.entity.EventStatus;
import com.entri.events.entity.TicketType;
import com.entri.events.mapper.EventMapper;
import com.entri.events.repository.EventRepository;
import com.entri.events.service.EventCategoryService;
import com.entri.events.service.EventService;
import com.entri.exception.BadRequestException;
import com.entri.exception.ForbiddenException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.security.UserPrincipal;
import com.entri.users.entity.Role;
import com.entri.users.entity.User;
import com.entri.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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
class EventServiceTest {

    @Mock EventMapper eventMapper;
    @Mock UserService userService;
    @Mock EventRepository eventRepository;
    @Mock EventCategoryService eventCategoryService;

    @InjectMocks EventService eventService;

    private static final String EVENT_EXTERNAL_ID = "evt-abc-123";

    private User buildUser(String externalKey, Role role) {
        return User.builder().externalKey(externalKey).role(role).build();
    }

    private Event buildEvent(Long id, User organizer, EventStatus status) {
        return Event.builder()
                .id(id)
                .externalId(EVENT_EXTERNAL_ID)
                .organizer(organizer)
                .status(status)
                .startTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(2, ChronoUnit.DAYS))
                .ticketTypes(List.of())
                .build();
    }

    private EventFilter buildFilter() {
        return new EventFilter(0, 10, "ASC", null, null, null, null);
    }

    private EventRequest buildRequest(Instant startTime, Instant endTime) {
        return new EventRequest("Title", "Description", 1L, "Venue", "Country", "City", startTime, endTime, "banner.png", "USD");
    }

    @Test
    @SuppressWarnings("unchecked")
    void listEvents_returnsMappedPaginationResponse() {
        var event = buildEvent(1L, buildUser("org-key", Role.ORGANIZER), EventStatus.PUBLISHED);
        var page = new PageImpl<>(List.of(event));
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        var response = new EventResponse(EVENT_EXTERNAL_ID, "Title", "Description", "Music", 1L, "Venue", "City", "Country", null, null, "banner.png", "USD", EventStatus.PUBLISHED, null, null);
        when(eventMapper.toEventResponse(event)).thenReturn(response);

        var result = eventService.listEvents(buildFilter());

        assertThat(result.content()).containsExactly(response);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void listOrganizerEvents_withOrganizerKey_returnsMappedPaginationResponse() {
        var event = buildEvent(1L, buildUser("org-key", Role.ORGANIZER), EventStatus.DRAFT);
        var page = new PageImpl<>(List.of(event));
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        var response = new EventResponse(EVENT_EXTERNAL_ID, "Title", "Description", "Music", 1L, "Venue", "City", "Country", null, null, "banner.png", "USD", EventStatus.DRAFT, null, null);
        when(eventMapper.toEventResponse(event)).thenReturn(response);

        var result = eventService.listOrganizerEvents(buildFilter(), "org-key");

        assertThat(result.content()).containsExactly(response);
    }

    @Test
    @SuppressWarnings("unchecked")
    void listOrganizerEvents_withNullOrganizerKey_stillReturnsResults() {
        var page = new PageImpl<Event>(List.of());
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = eventService.listOrganizerEvents(buildFilter(), null);

        assertThat(result.content()).isEmpty();
    }

    @Test
    void createEvent_startTimeInPast_throwsBadRequestException() {
        var request = buildRequest(Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS));

        assertThatThrownBy(() -> eventService.createEvent(request, "org-key"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Start time must not be in the past");
    }

    @Test
    void createEvent_endTimeBeforeStartTime_throwsBadRequestException() {
        var startTime = Instant.now().plus(2, ChronoUnit.DAYS);
        var endTime = startTime.minus(1, ChronoUnit.HOURS);
        var request = buildRequest(startTime, endTime);

        assertThatThrownBy(() -> eventService.createEvent(request, "org-key"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("End time must be after start time");
    }

    @Test
    void createEvent_validRequest_savesAndReturnsResponse() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var category = EventCategory.builder().id(1L).name("Music").build();
        when(userService.findEntityByExternalKey("org-key")).thenReturn(organizer);
        when(eventCategoryService.findById(1L)).thenReturn(category);
        var expected = new EventResponse(EVENT_EXTERNAL_ID, "Title", "Description", "Music", 1L, "Venue", "City", "Country", null, null, "banner.png", "USD", EventStatus.DRAFT, null, null);
        when(eventMapper.toEventResponse(any(Event.class))).thenReturn(expected);

        var startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        var endTime = startTime.plus(2, ChronoUnit.HOURS);
        var result = eventService.createEvent(buildRequest(startTime, endTime), "org-key");

        assertThat(result).isEqualTo(expected);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void getEventByExternalId_notFound_throwsResourceNotFoundException() {
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventByExternalId(EVENT_EXTERNAL_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(EVENT_EXTERNAL_ID);
    }

    @Test
    void getEventByExternalId_found_returnsMappedResponse() {
        var event = buildEvent(1L, buildUser("org-key", Role.ORGANIZER), EventStatus.PUBLISHED);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var expected = new EventResponse(EVENT_EXTERNAL_ID, "Title", "Description", "Music", 1L, "Venue", "City", "Country", null, null, "banner.png", "USD", EventStatus.PUBLISHED, null, null);
        when(eventMapper.toEventResponse(event)).thenReturn(expected);

        assertThat(eventService.getEventByExternalId(EVENT_EXTERNAL_ID)).isEqualTo(expected);
    }

    @Test
    void publishEvent_eventNotFound_throwsResourceNotFoundException() {
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.empty());
        var principal = new UserPrincipal(buildUser("org-key", Role.ORGANIZER));

        assertThatThrownBy(() -> eventService.publishEvent(EVENT_EXTERNAL_ID, principal))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void publishEvent_notOwnerAndNotAdmin_throwsForbiddenException() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer, EventStatus.DRAFT);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var otherUser = new UserPrincipal(buildUser("other-key", Role.ORGANIZER));

        assertThatThrownBy(() -> eventService.publishEvent(EVENT_EXTERNAL_ID, otherUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void publishEvent_noTicketTypes_throwsBadRequestException() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer, EventStatus.DRAFT);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var principal = new UserPrincipal(organizer);

        assertThatThrownBy(() -> eventService.publishEvent(EVENT_EXTERNAL_ID, principal))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no ticket types");
    }

    @Test
    void publishEvent_statusNotDraftOrCancelled_throwsBadRequestException() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer, EventStatus.PUBLISHED);
        event.setTicketTypes(List.of(TicketType.builder().id(1L).event(event).build()));
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var principal = new UserPrincipal(organizer);

        assertThatThrownBy(() -> eventService.publishEvent(EVENT_EXTERNAL_ID, principal))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("status is not DRAFT or CANCELLED");
    }

    @Test
    void publishEvent_startTimeInPast_throwsBadRequestException() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer, EventStatus.DRAFT);
        event.setStartTime(Instant.now().minus(1, ChronoUnit.DAYS));
        event.setTicketTypes(List.of(TicketType.builder().id(1L).event(event).build()));
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var principal = new UserPrincipal(organizer);

        assertThatThrownBy(() -> eventService.publishEvent(EVENT_EXTERNAL_ID, principal))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("start time is in the past");
    }

    @Test
    void publishEvent_validDraftEvent_setsPublishedStatusAndTimestamp() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer, EventStatus.DRAFT);
        event.setTicketTypes(List.of(TicketType.builder().id(1L).event(event).build()));
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var principal = new UserPrincipal(organizer);
        var expected = new EventResponse(EVENT_EXTERNAL_ID, "Title", "Description", "Music", 1L, "Venue", "City", "Country", null, null, "banner.png", "USD", EventStatus.PUBLISHED, Instant.now(), null);
        when(eventMapper.toEventResponse(event)).thenReturn(expected);

        var result = eventService.publishEvent(EVENT_EXTERNAL_ID, principal);

        assertThat(result).isEqualTo(expected);
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isNotNull();
    }

    @Test
    void publishEvent_validCancelledEvent_setsPublishedStatus() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer, EventStatus.CANCELLED);
        event.setTicketTypes(List.of(TicketType.builder().id(1L).event(event).build()));
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var principal = new UserPrincipal(organizer);
        when(eventMapper.toEventResponse(event)).thenReturn(
                new EventResponse(EVENT_EXTERNAL_ID, "Title", "Description", "Music", 1L, "Venue", "City", "Country", null, null, "banner.png", "USD", EventStatus.PUBLISHED, Instant.now(), null)
        );

        eventService.publishEvent(EVENT_EXTERNAL_ID, principal);

        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    void cancelEvent_notPublished_throwsBadRequestException() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer, EventStatus.DRAFT);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var principal = new UserPrincipal(organizer);

        assertThatThrownBy(() -> eventService.cancelEvent(EVENT_EXTERNAL_ID, principal))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("status is not PUBLISHED");
    }

    @Test
    void cancelEvent_notOwnerAndNotAdmin_throwsForbiddenException() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer, EventStatus.PUBLISHED);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var otherUser = new UserPrincipal(buildUser("other-key", Role.ORGANIZER));

        assertThatThrownBy(() -> eventService.cancelEvent(EVENT_EXTERNAL_ID, otherUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void cancelEvent_publishedEvent_setsCancelledStatus() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer, EventStatus.PUBLISHED);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var principal = new UserPrincipal(organizer);
        when(eventMapper.toEventResponse(event)).thenReturn(
                new EventResponse(EVENT_EXTERNAL_ID, "Title", "Description", "Music", 1L, "Venue", "City", "Country", null, null, "banner.png", "USD", EventStatus.CANCELLED, null, null)
        );

        eventService.cancelEvent(EVENT_EXTERNAL_ID, principal);

        assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
    }

    @Test
    void updateEvent_eventNotFound_throwsResourceNotFoundException() {
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.empty());
        var principal = new UserPrincipal(buildUser("org-key", Role.ORGANIZER));

        assertThatThrownBy(() -> eventService.updateEvent(EVENT_EXTERNAL_ID, buildRequest(Instant.now(), Instant.now().plus(1, ChronoUnit.DAYS)), principal))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateEvent_notOwnerAndNotAdmin_throwsForbiddenException() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer, EventStatus.DRAFT);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var otherUser = new UserPrincipal(buildUser("other-key", Role.ORGANIZER));

        var request = buildRequest(Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(2, ChronoUnit.DAYS));

        assertThatThrownBy(() -> eventService.updateEvent(EVENT_EXTERNAL_ID, request, otherUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void updateEvent_endTimeBeforeStartTime_throwsBadRequestException() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer, EventStatus.DRAFT);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var principal = new UserPrincipal(organizer);

        var startTime = Instant.now().plus(2, ChronoUnit.DAYS);
        var endTime = startTime.minus(1, ChronoUnit.HOURS);
        var request = buildRequest(startTime, endTime);

        assertThatThrownBy(() -> eventService.updateEvent(EVENT_EXTERNAL_ID, request, principal))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateEvent_startTimeInPastAllowedOnUpdate_doesNotThrow() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer, EventStatus.DRAFT);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var principal = new UserPrincipal(organizer);
        var category = EventCategory.builder().id(1L).name("Music").build();
        when(eventCategoryService.findById(1L)).thenReturn(category);
        when(eventMapper.toEventResponse(event)).thenReturn(
                new EventResponse(EVENT_EXTERNAL_ID, "Title", "Description", "Music", 1L, "Venue", "City", "Country", null, null, "banner.png", "USD", EventStatus.DRAFT, null, null)
        );

        var startTime = Instant.now().minus(1, ChronoUnit.DAYS);
        var endTime = startTime.plus(1, ChronoUnit.DAYS);
        var request = buildRequest(startTime, endTime);

        assertThat(eventService.updateEvent(EVENT_EXTERNAL_ID, request, principal)).isNotNull();
    }

    @Test
    void updateEvent_validRequest_updatesFieldsAndSaves() {
        var organizer = buildUser("org-key", Role.ORGANIZER);
        var event = buildEvent(1L, organizer, EventStatus.DRAFT);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        var principal = new UserPrincipal(organizer);
        var category = EventCategory.builder().id(2L).name("Sports").build();
        when(eventCategoryService.findById(2L)).thenReturn(category);
        when(eventMapper.toEventResponse(event)).thenReturn(
                new EventResponse(EVENT_EXTERNAL_ID, "New Title", "New Description", "Sports", 2L, "New Venue", "New City", "New Country", null, null, "new-banner.png", "EUR", EventStatus.DRAFT, null, null)
        );

        var startTime = Instant.now().plus(3, ChronoUnit.DAYS);
        var endTime = startTime.plus(1, ChronoUnit.DAYS);
        var request = new EventRequest("New Title", "New Description", 2L, "New Venue", "New Country", "New City", startTime, endTime, "new-banner.png", "EUR");

        eventService.updateEvent(EVENT_EXTERNAL_ID, request, principal);

        assertThat(event.getTitle()).isEqualTo("New Title");
        assertThat(event.getDescription()).isEqualTo("New Description");
        assertThat(event.getCategory()).isEqualTo(category);
        assertThat(event.getVenueName()).isEqualTo("New Venue");
        assertThat(event.getVenueCity()).isEqualTo("New City");
        assertThat(event.getVenueCountry()).isEqualTo("New Country");
        assertThat(event.getBannerUrl()).isEqualTo("new-banner.png");
        assertThat(event.getCurrency()).isEqualTo("EUR");
        verify(eventRepository).save(event);
    }
}
