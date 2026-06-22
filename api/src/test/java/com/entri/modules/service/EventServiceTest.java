package com.entri.modules.service;

import com.entri.common.dto.PaginationResponse;
import com.entri.common.exception.ForbiddenException;
import com.entri.common.exception.NotFoundException;
import com.entri.modules.events.dto.EventRequest;
import com.entri.modules.events.dto.CreateTicketTypeRequest;
import com.entri.modules.events.dto.EventDetailResponse;
import com.entri.modules.events.dto.EventFilter;
import com.entri.modules.events.dto.EventResponse;
import com.entri.modules.events.entity.Event;
import com.entri.modules.events.entity.EventCategory;
import com.entri.modules.events.entity.EventStatus;
import com.entri.modules.events.entity.TicketStatus;
import com.entri.modules.events.entity.TicketType;
import com.entri.modules.events.repository.EventRepository;
import com.entri.modules.events.service.EventCategoryService;
import com.entri.modules.events.service.EventService;
import com.entri.modules.events.service.mapper.EventMapper;
import com.entri.modules.users.entity.User;
import com.entri.modules.users.service.UserService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock EventMapper eventMapper;
    @Mock UserService userService;
    @Mock EventRepository eventRepository;
    @Mock
    EventCategoryService eventCategoryService;

    @InjectMocks
    EventService eventService;

    private static final String USER_KEY = "3d5e9973-7052-41e2-8a7f-13afc72fccb3";
    private static final String EVENT_EXTERNAL_ID = "550e8400-e29b-41d4-a716-446655440000";

    private User user;
    private EventCategory category;

    @BeforeEach
    void setUp() {
        user = User.builder().externalKey(USER_KEY).build();
        category = EventCategory.builder().id(1L).name("Music").build();
    }

    @Test
    void createEvent_savesEventWithCorrectFields() {
        when(userService.findEntityByExternalKey(USER_KEY)).thenReturn(user);
        when(eventCategoryService.findById(1L)).thenReturn(category);

        eventService.createEvent(baseRequest(List.of(singleTicket())), USER_KEY);

        Event saved = captureSavedEvent();
        assertThat(saved.getOrganizer()).isEqualTo(user);
        assertThat(saved.getTitle()).isEqualTo("Jazz Night");
        assertThat(saved.getDescription()).isEqualTo("A night to remember");
        assertThat(saved.getCategory()).isEqualTo(category);
        assertThat(saved.getVenueName()).isEqualTo("Uhuru Park");
        assertThat(saved.getVenueCountry()).isEqualTo("Kenya");
        assertThat(saved.getVenueCity()).isEqualTo("Nairobi");
        assertThat(saved.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(saved.isPublic()).isTrue();
    }

    @Test
    void createEvent_savesTicketTypeWithAllFields() {
        Instant saleStart = Instant.parse("2025-11-01T00:00:00Z");
        Instant saleEnd = Instant.parse("2025-12-14T23:59:59Z");
        var ticket = new CreateTicketTypeRequest(
                "VIP", "Front row access", new BigDecimal("5000.00"), "KES",
                50, 2, saleStart, saleEnd, 1, false
        );
        when(userService.findEntityByExternalKey(USER_KEY)).thenReturn(user);
        when(eventCategoryService.findById(1L)).thenReturn(category);

        eventService.createEvent(baseRequest(List.of(ticket)), USER_KEY);

        TicketType saved = captureSavedEvent().getTicketTypes().get(0);
        assertThat(saved.getName()).isEqualTo("VIP");
        assertThat(saved.getDescription()).isEqualTo("Front row access");
        assertThat(saved.getPrice()).isEqualByComparingTo("5000.00");
        assertThat(saved.getCurrency()).isEqualTo("KES");
        assertThat(saved.getQuantity()).isEqualTo(50);
        assertThat(saved.getMaxPerOrder()).isEqualTo(2);
        assertThat(saved.getSaleStartDate()).isEqualTo(saleStart);
        assertThat(saved.getSaleEndDate()).isEqualTo(saleEnd);
        assertThat(saved.getDisplayOrder()).isEqualTo(1);
        assertThat(saved.isHidden()).isFalse();
        assertThat(saved.getStatus()).isEqualTo(TicketStatus.ACTIVE);
    }

    @Test
    void createEvent_ticketType_linksBackToParentEvent() {
        when(userService.findEntityByExternalKey(USER_KEY)).thenReturn(user);
        when(eventCategoryService.findById(1L)).thenReturn(category);

        eventService.createEvent(baseRequest(List.of(singleTicket())), USER_KEY);

        Event saved = captureSavedEvent();
        assertThat(saved.getTicketTypes().get(0).getEvent()).isSameAs(saved);
    }

    @Test
    void createEvent_ticketType_nullDisplayOrder_defaultsToZero() {
        var ticket = new CreateTicketTypeRequest(
                "General", null, new BigDecimal("500.00"), "KES", 100, null, null, null, null, false
        );
        when(userService.findEntityByExternalKey(USER_KEY)).thenReturn(user);
        when(eventCategoryService.findById(1L)).thenReturn(category);

        eventService.createEvent(baseRequest(List.of(ticket)), USER_KEY);

        assertThat(captureSavedEvent().getTicketTypes().get(0).getDisplayOrder()).isZero();
    }

    @Test
    void createEvent_ticketType_nullIsHidden_defaultsToFalse() {
        var ticket = new CreateTicketTypeRequest(
                "General", null, new BigDecimal("500.00"), "KES", 100, null, null, null, 0, null
        );
        when(userService.findEntityByExternalKey(USER_KEY)).thenReturn(user);
        when(eventCategoryService.findById(1L)).thenReturn(category);

        eventService.createEvent(baseRequest(List.of(ticket)), USER_KEY);

        assertThat(captureSavedEvent().getTicketTypes().get(0).isHidden()).isFalse();
    }

    @Test
    void createEvent_multipleTicketTypes_allSaved() {
        var tickets = List.of(
                new CreateTicketTypeRequest("General", null, new BigDecimal("1000.00"), "KES", 200, null, null, null, 0, false),
                new CreateTicketTypeRequest("VIP", null, new BigDecimal("5000.00"), "KES", 50, null, null, null, 1, false)
        );
        when(userService.findEntityByExternalKey(USER_KEY)).thenReturn(user);
        when(eventCategoryService.findById(1L)).thenReturn(category);

        eventService.createEvent(baseRequest(tickets), USER_KEY);

        assertThat(captureSavedEvent().getTicketTypes()).hasSize(2);
    }

    @Test
    void createEvent_nullTicketTypes_savesEventWithNoTickets() {
        when(userService.findEntityByExternalKey(USER_KEY)).thenReturn(user);
        when(eventCategoryService.findById(1L)).thenReturn(category);

        eventService.createEvent(baseRequest(null), USER_KEY);

        assertThat(captureSavedEvent().getTicketTypes()).isNullOrEmpty();
    }

    @Test
    void createEvent_emptyTicketTypes_savesEventWithNoTickets() {
        when(userService.findEntityByExternalKey(USER_KEY)).thenReturn(user);
        when(eventCategoryService.findById(1L)).thenReturn(category);

        eventService.createEvent(baseRequest(List.of()), USER_KEY);

        assertThat(captureSavedEvent().getTicketTypes()).isNullOrEmpty();
    }

    @Test
    void createEvent_nullIsPublic_defaultsToPublic() {
        when(userService.findEntityByExternalKey(USER_KEY)).thenReturn(user);
        when(eventCategoryService.findById(1L)).thenReturn(category);

        eventService.createEvent(requestWithIsPublic(null), USER_KEY);

        assertThat(captureSavedEvent().isPublic()).isTrue();
    }

    @Test
    void createEvent_isPublicFalse_savesPrivateEvent() {
        when(userService.findEntityByExternalKey(USER_KEY)).thenReturn(user);
        when(eventCategoryService.findById(1L)).thenReturn(category);

        eventService.createEvent(requestWithIsPublic(false), USER_KEY);

        assertThat(captureSavedEvent().isPublic()).isFalse();
    }

    @Test
    void createEvent_returnsMapperResponse() {
        var expected = new EventResponse(
                EVENT_EXTERNAL_ID, "Jazz Night", "A night to remember", "Music",
                "Uhuru Park", "Kenya", "Nairobi",
                Instant.parse("2025-12-15T19:00:00Z"), Instant.parse("2025-12-15T23:00:00Z"),
                "https://example.com/banner.jpg", EventStatus.DRAFT, true, null,
                Instant.parse("2025-12-01T10:00:00Z")
        );
        when(userService.findEntityByExternalKey(USER_KEY)).thenReturn(user);
        when(eventCategoryService.findById(1L)).thenReturn(category);
        when(eventMapper.toEventResponse(any(Event.class))).thenReturn(expected);

        var result = eventService.createEvent(baseRequest(List.of(singleTicket())), USER_KEY);

        verify(eventMapper).toEventResponse(any(Event.class));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getEvents_noEventsExist_returnsEmptyList() {
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        PaginationResponse<EventResponse> result = eventService.getEvents(filter());

        assertThat(result.content()).isEmpty();
    }

    @Test
    void getEvents_eventsExist_shouldReturnPaginatedEvents() {
        List<Event> events = List.of(
            Event.builder().id(1L).title("Jazz Night").build(),
            Event.builder().id(2L).title("Tech Meetup").build()
        );
        EventResponse r1 = new EventResponse(
                "id1", "Jazz Night", null, null, null, null, null,
                null, null, null, null, false, null, null);
        EventResponse r2 = new EventResponse(
                "id2", "Tech Meetup", null, null, null, null, null,
                null, null, null, null, false, null, null);

        Page<Event> page = new PageImpl<>(events, PageRequest.of(0, 2), 5);

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(eventMapper.toEventResponse(events.get(0))).thenReturn(r1);
        when(eventMapper.toEventResponse(events.get(1))).thenReturn(r2);

        PaginationResponse<EventResponse> result = eventService.getEvents(new EventFilter(0, 2, null, null, null, null, null));

        assertThat(result.content()).containsExactly(r1, r2);
        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.pageNumber()).isEqualTo(0);
        assertThat(result.pageSize()).isEqualTo(2);
        assertThat(result.first()).isTrue();
        assertThat(result.last()).isFalse();
    }

    @Test
    void getEvents_lastPage_shouldReturnCorrectMetadata() {
        List<Event> lastPageContent = List.of(Event.builder().id(5L).build());
        Page<Event> page = new PageImpl<>(lastPageContent, PageRequest.of(2, 2), 5);

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(eventMapper.toEventResponse(any(Event.class))).thenReturn(mock(EventResponse.class));

        PaginationResponse<EventResponse> result = eventService.getEvents(new EventFilter(2, 2, null, null, null, null, null));

        assertThat(result.pageNumber()).isEqualTo(2);
        assertThat(result.pageSize()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.first()).isFalse();
        assertThat(result.last()).isTrue();
    }

    @Test
    void getEvents_nullPageAndSize_usesDefaultPagination() {
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        eventService.getEvents(new EventFilter(null, null, null, null, null, null, null));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(eventRepository).findAll(any(Specification.class), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    void getEvents_nullSortDirection_defaultsToAscendingStartTime() {
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        eventService.getEvents(filter());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(eventRepository).findAll(any(Specification.class), captor.capture());
        assertThat(captor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "startTime"));
    }

    @Test
    void getEvents_descSortDirection_sortsStartTimeDescending() {
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        eventService.getEvents(new EventFilter(0, 10, "DESC", null, null, null, null));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(eventRepository).findAll(any(Specification.class), captor.capture());
        assertThat(captor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "startTime"));
    }

    @Test
    void getEvents_alwaysCallsFindAllWithSpecification() {
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        eventService.getEvents(filter());

        verify(eventRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(eventRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getEvents_withStartFrom_passesSpecificationToRepository() {
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        eventService.getEvents(new EventFilter(0, 10, null, null, null, "2026-06-14T00:00:00.000Z", null));

        verify(eventRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getEvents_withStartTo_passesSpecificationToRepository() {
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        eventService.getEvents(new EventFilter(0, 10, null, null, null, null, "2026-06-17T23:59:59.999Z"));

        verify(eventRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getEvents_withBothDateFilters_passesSpecificationToRepository() {
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        eventService.getEvents(new EventFilter(0, 10, null, null, null,
                "2026-06-14T00:00:00.000Z", "2026-06-17T23:59:59.999Z"));

        verify(eventRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getEvents_nullDateFilters_doesNotApplyDateRange() {
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        eventService.getEvents(filter());

        verify(eventRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getEventsForManagement_nullOrganizerKey_returnsAllEvents() {
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        PaginationResponse<EventResponse> result = eventService.getEventsForManagement(filter(), null);

        assertThat(result.content()).isEmpty();
        verify(eventRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getEventsForManagement_withOrganizerKey_passesOrganizerFilterToRepository() {
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        eventService.getEventsForManagement(filter(), USER_KEY);

        verify(eventRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getEventsForManagement_returnsCorrectPaginationMetadata() {
        List<Event> events = List.of(Event.builder().id(1L).build());
        Page<Event> page = new PageImpl<>(events, PageRequest.of(0, 10), 1);
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(eventMapper.toEventResponse(any(Event.class))).thenReturn(mock(EventResponse.class));

        PaginationResponse<EventResponse> result = eventService.getEventsForManagement(filter(), null);

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.first()).isTrue();
        assertThat(result.last()).isTrue();
    }

    @Test
    void getEventsForManagement_mapsEachEventUsingMapper() {
        List<Event> events = List.of(Event.builder().id(1L).build(), Event.builder().id(2L).build());
        Page<Event> page = new PageImpl<>(events, PageRequest.of(0, 10), 2);
        EventResponse r1 = new EventResponse("id1", "Draft Event", null, null, null, null, null, null, null, null, null, false, null, null);
        EventResponse r2 = new EventResponse("id2", "Private Event", null, null, null, null, null, null, null, null, null, false, null, null);
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(eventMapper.toEventResponse(events.get(0))).thenReturn(r1);
        when(eventMapper.toEventResponse(events.get(1))).thenReturn(r2);

        PaginationResponse<EventResponse> result = eventService.getEventsForManagement(filter(), null);

        assertThat(result.content()).containsExactly(r1, r2);
    }

    @Test
    void updateEvent_eventNotFound_throwsNotFoundException() {
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEvent(EVENT_EXTERNAL_ID, baseRequest(null), USER_KEY))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with external ID: " + EVENT_EXTERNAL_ID + " not found");
    }

    @Test
    void updateEvent_callerIsNotOrganizer_throwsForbiddenException() {
        User otherUser = User.builder().externalKey("other-user-key").build();
        Event event = Event.builder().organizer(otherUser).build();
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.updateEvent(EVENT_EXTERNAL_ID, baseRequest(null), USER_KEY))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void updateEvent_updatesAllScalarFields() {
        Event event = Event.builder().organizer(user).build();
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(eventCategoryService.findById(1L)).thenReturn(category);

        eventService.updateEvent(EVENT_EXTERNAL_ID, baseRequest(null), USER_KEY);

        assertThat(event.getTitle()).isEqualTo("Jazz Night");
        assertThat(event.getDescription()).isEqualTo("A night to remember");
        assertThat(event.getVenueName()).isEqualTo("Uhuru Park");
        assertThat(event.getVenueCountry()).isEqualTo("Kenya");
        assertThat(event.getVenueCity()).isEqualTo("Nairobi");
        assertThat(event.getCategory()).isEqualTo(category);
        assertThat(event.isPublic()).isTrue();
    }

    @Test
    void updateEvent_nullIsPublic_preservesExistingValue() {
        Event event = Event.builder().organizer(user).isPublic(false).build();
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(eventCategoryService.findById(1L)).thenReturn(category);

        eventService.updateEvent(EVENT_EXTERNAL_ID, requestWithIsPublic(null), USER_KEY);

        assertThat(event.isPublic()).isFalse();
    }

    @Test
    void updateEvent_returnsMapperResponse() {
        Event event = Event.builder().organizer(user).build();
        EventResponse expected = new EventResponse(
                EVENT_EXTERNAL_ID, "Jazz Night", "A night to remember", "Music",
                "Uhuru Park", "Kenya", "Nairobi",
                Instant.parse("2025-12-15T19:00:00Z"), Instant.parse("2025-12-15T23:00:00Z"),
                "https://example.com/banner.jpg", EventStatus.DRAFT, true, null,
                Instant.parse("2025-12-01T10:00:00Z")
        );
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(eventCategoryService.findById(1L)).thenReturn(category);
        when(eventMapper.toEventResponse(event)).thenReturn(expected);

        var result = eventService.updateEvent(EVENT_EXTERNAL_ID, baseRequest(null), USER_KEY);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getEventByExternalId_eventExists_returnsEventDetailResponse() {
        Event event = Event.builder().build();
        EventDetailResponse expected = new EventDetailResponse(
                EVENT_EXTERNAL_ID, "Jazz Night", "A great show", "Music",
                "KICC", "Nairobi", "Kenya", null, null, null,
                EventStatus.DRAFT, true, null, null, List.of()
        );
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));
        when(eventMapper.toEventDetailResponse(event)).thenReturn(expected);

        assertThat(eventService.getEventByExternalId(EVENT_EXTERNAL_ID)).isEqualTo(expected);
    }

    @Test
    void getEventByExternalId_eventNotFound_throwsNotFoundException() {
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventByExternalId(EVENT_EXTERNAL_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with external ID: " + EVENT_EXTERNAL_ID + " not found");
    }

    private Event captureSavedEvent() {
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        return captor.getValue();
    }

    private EventRequest baseRequest(List<CreateTicketTypeRequest> ticketTypes) {
        return new EventRequest(
                "Jazz Night", "A night to remember",
                1L, "Uhuru Park", "Kenya", "Nairobi",
                Instant.parse("2025-12-15T19:00:00Z"), Instant.parse("2025-12-15T23:00:00Z"),
                "https://example.com/banner.jpg", true, ticketTypes
        );
    }

    private EventRequest requestWithIsPublic(Boolean isPublic) {
        return new EventRequest(
                "Jazz Night", "A night to remember",
                1L, "Uhuru Park", "Kenya", "Nairobi",
                Instant.parse("2025-12-15T19:00:00Z"), Instant.parse("2025-12-15T23:00:00Z"),
                "https://example.com/banner.jpg", isPublic, null
        );
    }

    private CreateTicketTypeRequest singleTicket() {
        return new CreateTicketTypeRequest(
                "General Admission", "Standard entry",
                new BigDecimal("1500.00"), "KES",
                200, null, null, null, 0, false
        );
    }

    private EventFilter filter() {
        return new EventFilter(0, 10, null, null, null, null, null);
    }
}
