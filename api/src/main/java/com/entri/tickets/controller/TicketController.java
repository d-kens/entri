package com.entri.tickets.controller;

import com.entri.tickets.controller.api.TicketApi;
import com.entri.tickets.dto.CheckInRequest;
import com.entri.tickets.dto.CheckInResponse;
import com.entri.tickets.dto.TicketResponse;
import com.entri.tickets.dto.VerifyCodeRequest;
import com.entri.tickets.dto.VerifyCodeResponse;
import com.entri.tickets.service.CheckInCodeService;
import com.entri.tickets.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TicketController implements TicketApi {

    private final TicketService ticketService;
    private final CheckInCodeService checkInCodeService;

    @Override
    public List<TicketResponse> getTicketsByReservation(String reservationId) {
        return ticketService.getTicketsByReservation(reservationId);
    }

    @Override
    public TicketResponse getTicket(String externalId) {
        return ticketService.getTicket(externalId);
    }

    @Override
    public VerifyCodeResponse verifyCheckInCode(VerifyCodeRequest request) {
        return checkInCodeService.verifyCode(request);
    }

    @Override
    public CheckInResponse checkIn(String ticketCode, CheckInRequest request) {
        return ticketService.checkIn(ticketCode, request);
    }
}

