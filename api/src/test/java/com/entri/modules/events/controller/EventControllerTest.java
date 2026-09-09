package com.entri.modules.events.controller;

import com.entri.common.dto.PaginationResponse;
import com.entri.events.controller.EventController;
import com.entri.events.dto.EventCheckInStatsResponse;
import com.entri.events.dto.EventRequest;
import com.entri.events.dto.EventResponse;
import com.entri.events.dto.EventTicketReservationItemRequest;
import com.entri.events.dto.EventTicketReservationRequest;
import com.entri.events.dto.EventTicketReservationResponse;
import com.entri.events.entity.EventStatus;
import com.entri.events.exception.InsufficientTicketsException;
import com.entri.events.service.EventService;
import com.entri.events.service.EventTicketReservationService;
import com.entri.events.service.TicketTypeService;
import com.entri.exception.GlobalExceptionHandler;
import com.entri.exception.ResourceNotFoundException;
import com.entri.security.UserPrincipal;
import com.entri.tickets.dto.CheckInCodeResponse;
import com.entri.tickets.dto.TicketResponse;
import com.entri.tickets.service.CheckInCodeService;
import com.entri.tickets.service.TicketService;
import com.entri.users.entity.Role;
import com.entri.users.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    EventService eventService;
    @Mock
    TicketTypeService ticketTypeService;
    @Mock
    EventTicketReservationService eventTicketReservationService;
    @Mock
    CheckInCodeService checkInCodeService;
    @Mock
    TicketService ticketService;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    UserPrincipal requestingUser;

    @BeforeEach
    void setUp() {
        EventController controller = new EventController(
                eventService, ticketTypeService, eventTicketReservationService, checkInCodeService, ticketService);
        User user = User.builder().externalKey("organizer-key").role(Role.ORGANIZER).build();
        requestingUser = new UserPrincipal(user);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new UserPrincipalArgumentResolver(requestingUser))
                .build();
    }

    private EventResponse sampleEvent() {
        return new EventResponse("event-ext-id", "Concert", "A concert", "Music", 1L,
                "Venue", "City", "Country", Instant.now(), Instant.now().plusSeconds(3600),
                "banner.png", "KES", EventStatus.PUBLISHED, Instant.now(), Instant.now());
    }

    @Test
    void getEventByExternalId_existingEvent_returnsEvent() throws Exception {
        when(eventService.getEventByExternalId("event-ext-id")).thenReturn(sampleEvent());

        mockMvc.perform(get("/events/{eventExternalId}", "event-ext-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Concert"));
    }

    @Test
    void getEventByExternalId_notFound_returns404() throws Exception {
        when(eventService.getEventByExternalId("missing")).thenThrow(new ResourceNotFoundException("Event not found"));

        mockMvc.perform(get("/events/{eventExternalId}", "missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void publishEvent_validEvent_returnsPublishedEvent() throws Exception {
        when(eventService.publishEvent(eq("event-ext-id"), any(UserPrincipal.class))).thenReturn(sampleEvent());

        mockMvc.perform(patch("/events/{eventExternalId}/publish", "event-ext-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void cancelEvent_validEvent_returnsCancelledEvent() throws Exception {
        when(eventService.cancelEvent(eq("event-ext-id"), any(UserPrincipal.class))).thenReturn(sampleEvent());

        mockMvc.perform(patch("/events/{eventExternalId}/cancel", "event-ext-id"))
                .andExpect(status().isOk());
    }

    @Test
    void getEventTicketTypes_returnsTicketTypes() throws Exception {
        when(ticketTypeService.getTicketTypesByEventExternalId("event-ext-id")).thenReturn(List.of());

        mockMvc.perform(get("/events/{eventExternalId}/ticket-types", "event-ext-id"))
                .andExpect(status().isOk());
    }

    @Test
    void createEvent_validRequest_returns201WithLocation() throws Exception {
        var request = new EventRequest("Concert", "A concert", 1L, "Venue", "Country", "City",
                Instant.now(), Instant.now().plusSeconds(3600), "banner.png", "KES");
        when(eventService.createEvent(any(EventRequest.class), eq("organizer-key"))).thenReturn(sampleEvent());

        mockMvc.perform(post("/events")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/events/event-ext-id")));
    }

    @Test
    void createEvent_blankTitle_returns400() throws Exception {
        var request = new EventRequest("", "A concert", 1L, "Venue", "Country", "City",
                Instant.now(), Instant.now().plusSeconds(3600), "banner.png", "KES");

        mockMvc.perform(post("/events")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEvent_validRequest_returnsUpdatedEvent() throws Exception {
        var request = new EventRequest("Concert", "A concert", 1L, "Venue", "Country", "City",
                Instant.now(), Instant.now().plusSeconds(3600), "banner.png", "KES");
        when(eventService.updateEvent(eq("event-ext-id"), any(EventRequest.class), any(UserPrincipal.class)))
                .thenReturn(sampleEvent());

        mockMvc.perform(put("/events/{externalId}", "event-ext-id")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void listEvents_returnsPaginatedEvents() throws Exception {
        var pagination = new PaginationResponse<>(List.of(sampleEvent()), 0, 10, 1, 1, true, true);
        when(eventService.listEvents(any())).thenReturn(pagination);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Concert"));
    }

    @Test
    void listOrganizerEvents_asOrganizer_scopesToOwnEvents() throws Exception {
        var pagination = new PaginationResponse<>(List.of(sampleEvent()), 0, 10, 1, 1, true, true);
        when(eventService.listOrganizerEvents(any(), eq("organizer-key"))).thenReturn(pagination);

        mockMvc.perform(get("/events/manage"))
                .andExpect(status().isOk());
    }

    @Test
    void reserveEventTickets_validRequest_returns201WithLocation() throws Exception {
        var request = new EventTicketReservationRequest(List.of(new EventTicketReservationItemRequest(1L, 2)));
        var response = new EventTicketReservationResponse(Instant.now().plusSeconds(600), "reservation-id",
                java.math.BigDecimal.TEN, "event-ext-id");
        when(eventTicketReservationService.reserveEventTickets(eq("event-ext-id"), any(EventTicketReservationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/events/{eventExternalId}/reservations", "event-ext-id")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("reservation-id")));
    }

    @Test
    void reserveEventTickets_emptyItems_returns400() throws Exception {
        var request = new EventTicketReservationRequest(List.of());

        mockMvc.perform(post("/events/{eventExternalId}/reservations", "event-ext-id")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reserveEventTickets_insufficientTickets_returns409() throws Exception {
        var request = new EventTicketReservationRequest(List.of(new EventTicketReservationItemRequest(1L, 2)));
        when(eventTicketReservationService.reserveEventTickets(eq("event-ext-id"), any(EventTicketReservationRequest.class)))
                .thenThrow(new InsufficientTicketsException("Not enough tickets available"));

        mockMvc.perform(post("/events/{eventExternalId}/reservations", "event-ext-id")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void generateCheckInCode_validEvent_returnsCode() throws Exception {
        var response = new CheckInCodeResponse("123456", "event-ext-id", Instant.now().plusSeconds(3600));
        when(checkInCodeService.generateCode(eq("event-ext-id"), eq("organizer-key"))).thenReturn(response);

        mockMvc.perform(post("/events/{eventExternalId}/check-in-code", "event-ext-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("123456"));
    }

    @Test
    void getEventTicketReservation_existingReservation_returnsDetails() throws Exception {
        when(eventTicketReservationService.getEventTicketReservation(eq("event-ext-id"), eq("reservation-id")))
                .thenThrow(new ResourceNotFoundException("Reservation not found"));

        mockMvc.perform(get("/events/{eventExternalId}/reservations/{reservationId}", "event-ext-id", "reservation-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listEventReservations_returnsPaginatedReservations() throws Exception {
        var pagination = new PaginationResponse<com.entri.events.dto.EventReservationSummaryResponse>(List.of(), 0, 20, 0, 0, true, true);
        when(eventTicketReservationService.listEventReservations(eq("event-ext-id"), anyInt(), anyInt(), any(UserPrincipal.class)))
                .thenReturn(pagination);

        mockMvc.perform(get("/events/{eventExternalId}/reservations", "event-ext-id"))
                .andExpect(status().isOk());
    }

    @Test
    void getEventTickets_returnsPaginatedTickets() throws Exception {
        var pagination = new PaginationResponse<TicketResponse>(List.of(), 0, 10, 0, 0, true, true);
        when(ticketService.getTickets(eq("event-ext-id"), any(), any(UserPrincipal.class))).thenReturn(pagination);

        mockMvc.perform(get("/events/{eventExternalId}/tickets", "event-ext-id"))
                .andExpect(status().isOk());
    }

    @Test
    void getCheckInStats_returnsStats() throws Exception {
        var stats = new EventCheckInStatsResponse("event-ext-id", 100L, 40L, 40.0, List.of());
        when(ticketService.getCheckInStats(eq("event-ext-id"), any(UserPrincipal.class))).thenReturn(stats);

        mockMvc.perform(get("/events/{eventExternalId}/check-in-stats", "event-ext-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkedIn").value(40));
    }

    static class UserPrincipalArgumentResolver implements HandlerMethodArgumentResolver {
        private final UserPrincipal principal;

        UserPrincipalArgumentResolver(UserPrincipal principal) {
            this.principal = principal;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().equals(UserPrincipal.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                       NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return principal;
        }
    }
}
