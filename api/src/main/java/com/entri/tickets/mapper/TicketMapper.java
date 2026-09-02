package com.entri.tickets.mapper;

import com.entri.tickets.dto.TicketResponse;
import com.entri.tickets.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "eventExternalId", source = "event.externalId")
    @Mapping(target = "eventTitle", source = "event.title")
    @Mapping(target = "ticketTypeName", source = "ticketType.name")
    @Mapping(target = "holderFirstName", source = "reservation.firstName")
    @Mapping(target = "holderLastName", source = "reservation.lastName")
    @Mapping(target = "status", source = "status")
    TicketResponse toResponse(Ticket ticket);
}
