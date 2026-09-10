package com.entri.modules.tickets.controller;

import com.entri.exception.GlobalExceptionHandler;
import com.entri.exception.ResourceNotFoundException;
import com.entri.tickets.controller.TicketController;
import com.entri.tickets.dto.CheckInRequest;
import com.entri.tickets.dto.CheckInResponse;
import com.entri.tickets.dto.CheckInResult;
import com.entri.tickets.dto.TicketResponse;
import com.entri.tickets.dto.VerifyCodeRequest;
import com.entri.tickets.dto.VerifyCodeResponse;
import com.entri.tickets.service.CheckInCodeService;
import com.entri.tickets.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private CheckInCodeService checkInCodeService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TicketController(ticketService, checkInCodeService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getTicketsByReservation_returnsTicketList() throws Exception {
        var ticket = new TicketResponse("ticket-1", "CODE-1", "res-1", "event-1", "Concert",
                null, null, "Venue", "Nairobi", "KES", "VIP", java.math.BigDecimal.TEN,
                "Jane", "Doe", "VALID", null);
        when(ticketService.getTicketsByReservation("res-1")).thenReturn(List.of(ticket));

        mockMvc.perform(get("/reservations/res-1/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketExternalId").value("ticket-1"));
    }

    @Test
    void getTicketsByReservation_reservationNotFound_returnsNotFound() throws Exception {
        when(ticketService.getTicketsByReservation("missing"))
                .thenThrow(new ResourceNotFoundException("Reservation not found"));

        mockMvc.perform(get("/reservations/missing/tickets"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTicket_returnsTicket() throws Exception {
        var ticket = new TicketResponse("ticket-1", "CODE-1", "res-1", "event-1", "Concert",
                null, null, "Venue", "Nairobi", "KES", "VIP", java.math.BigDecimal.TEN,
                "Jane", "Doe", "VALID", null);
        when(ticketService.getTicket("ticket-1")).thenReturn(ticket);

        mockMvc.perform(get("/tickets/ticket-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketCode").value("CODE-1"));
    }

    @Test
    void getTicket_notFound_returnsNotFound() throws Exception {
        when(ticketService.getTicket("missing")).thenThrow(new ResourceNotFoundException("Ticket not found"));

        mockMvc.perform(get("/tickets/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void verifyCheckInCode_validCode_returnsEventDetails() throws Exception {
        when(checkInCodeService.verifyCode(new VerifyCodeRequest("ABC123")))
                .thenReturn(new VerifyCodeResponse("event-1", "Concert"));

        mockMvc.perform(post("/check-in/verify-code")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new VerifyCodeRequest("ABC123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventTitle").value("Concert"));
    }

    @Test
    void verifyCheckInCode_blankCode_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/check-in/verify-code")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new VerifyCodeRequest(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkIn_validTicketAndCode_returnsCheckInResult() throws Exception {
        var request = new CheckInRequest("ABC123");
        when(ticketService.checkIn(eq("TICKET-CODE"), eq(request)))
                .thenReturn(new CheckInResponse(CheckInResult.VALID, "Jane Doe", "VIP", java.time.Instant.now()));

        mockMvc.perform(post("/tickets/TICKET-CODE/check-in")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("VALID"))
                .andExpect(jsonPath("$.holderName").value("Jane Doe"));
    }

    @Test
    void checkIn_blankCode_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/tickets/TICKET-CODE/check-in")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CheckInRequest(""))))
                .andExpect(status().isBadRequest());
    }
}
