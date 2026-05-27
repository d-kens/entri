package com.api.modules.events.service;

import com.api.common.exception.NotFoundException;
import com.api.modules.events.dto.CreateEventRequest;
import com.api.modules.events.dto.CreateTicketTypeRequest;
import com.api.modules.events.dto.EventDetailResponse;
import com.api.modules.events.dto.EventResponse;
import com.api.modules.events.entity.Event;
import com.api.modules.events.entity.EventCategory;
import com.api.modules.events.entity.EventStatus;
import com.api.modules.events.repository.EventRepository;
import com.api.modules.events.service.mapper.EventMapper;
import com.api.modules.users.entity.User;
import com.api.modules.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
    @Mock EventMapper eventMapper;
    @Mock UserService userService;
    @Mock EventRepository eventRepository;
    @Mock EventCategoryService eventCategoryService;

    @InjectMocks EventService eventService;


    @Test
    void createEvent_validEventRequestWithTicketTypes_returnsEventResponse() {
        Long categoryId = 1L;
        String eventExternalId = "550e8400-e29b-41d4-a716-446655440000";
        String currentUserKey = "3d5e9973-7052-41e2-8a7f-13afc72fccb3";
        EventCategory category = EventCategory.builder().id(categoryId).name("Music").build();
        User user = User.builder().externalKey(currentUserKey).build();

        List<CreateTicketTypeRequest> ticketTypes = List.of(
               new CreateTicketTypeRequest(
                       "General Admission", "Standard entry ticket",
                       new BigDecimal("1500.00"), "KES",
                       200, null, null, null, 0, false
               )
        );

        var request = new CreateEventRequest(
                "Jazz Night", "This will be an event like no other. Don't fail to attend",
                categoryId, "Uhuru Park", "Kenya", "Nairobi", Instant.parse("2025-12-15T19:00:00Z"), Instant.parse("2025-12-15T23:00:00Z"),
                "https://storage.googleapis.com/oro-web-app.firebasestorage.app/3bc6569f-8a7e-4d40-ba34-db0d7de20676_images.jpeg",
                true, ticketTypes
        );

        var expectedResponse = new EventResponse(
                eventExternalId, "Jazz Night", "This will be an event like no other. Don't fail to attend", "Music",
                "Uhuru Park", "Kenya", "Nairobi", Instant.parse("2025-12-15T19:00:00Z"), Instant.parse("2025-12-15T23:00:00Z"),
                "https://storage.googleapis.com/oro-web-app.firebasestorage.app/3bc6569f-8a7e-4d40-ba34-db0d7de20676_images.jpeg", EventStatus.DRAFT, true, null, Instant.parse("2025-12-01T10:00:00Z")
        );

        when(userService.findEntityByExternalKey(currentUserKey)).thenReturn(user);
        when(eventCategoryService.findById(categoryId)).thenReturn(category);
        when(eventMapper.toEventResponse(any(Event.class))).thenReturn(expectedResponse);

        var result = eventService.createEvent(request, currentUserKey);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();
        assertThat(savedEvent.getOrganizer()).isEqualTo(user);
        assertThat(savedEvent.getTitle()).isEqualTo("Jazz Night");
        assertThat(savedEvent.getCategory()).isEqualTo(category);
        assertThat(savedEvent.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(savedEvent.isPublic()).isTrue();
        assertThat(savedEvent.getTicketTypes()).hasSize(1);

        var savedTicket = savedEvent.getTicketTypes().get(0);
        assertThat(savedTicket.getName()).isEqualTo("General Admission");
        assertThat(savedTicket.getPrice()).isEqualByComparingTo("1500.00");
        assertThat(savedTicket.getCurrency()).isEqualTo("KES");
        assertThat(savedTicket.getQuantity()).isEqualTo(200);
        assertThat(savedTicket.getEvent()).isEqualTo(savedEvent);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getEventByExternalId_eventExists_returnsEventDetailsResponse() {
        String externalId = "550e8400-e29b-41d4-a716-446655440000";
        Event event = Event.builder().build();
        EventDetailResponse expectedResponse = new EventDetailResponse(
                externalId, "Jazz Night", "A great show", "Music",
                "KICC", "Nairobi", "Kenya", null, null, null,
                EventStatus.DRAFT, true, null, null, List.of()
        );

        when(eventRepository.findByExternalId(externalId)).thenReturn(Optional.of(event));
        when(eventMapper.toEventDetailResponse(event)).thenReturn(expectedResponse);

        EventDetailResponse result = eventService.getEventByExternalId(externalId);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getEventByExternalId_eventNotFound_throwsNotFoundException() {
        String externalId = "550e8400-e29b-41d4-a716-446655440000";

        when(eventRepository.findByExternalId(externalId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventByExternalId(externalId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with external ID: 550e8400-e29b-41d4-a716-446655440000 not found");
    }
}
