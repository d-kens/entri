package com.entri.modules.service;

import com.entri.common.exception.NotFoundException;
import com.entri.modules.events.dto.CreateEventRequest;
import com.entri.modules.events.dto.CreateTicketTypeRequest;
import com.entri.modules.events.dto.EventDetailResponse;
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

import java.math.BigDecimal;
import java.time.Instant;
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

    private CreateEventRequest baseRequest(List<CreateTicketTypeRequest> ticketTypes) {
        return new CreateEventRequest(
                "Jazz Night", "A night to remember",
                1L, "Uhuru Park", "Kenya", "Nairobi",
                Instant.parse("2025-12-15T19:00:00Z"), Instant.parse("2025-12-15T23:00:00Z"),
                "https://example.com/banner.jpg", true, ticketTypes
        );
    }

    private CreateEventRequest requestWithIsPublic(Boolean isPublic) {
        return new CreateEventRequest(
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
}
