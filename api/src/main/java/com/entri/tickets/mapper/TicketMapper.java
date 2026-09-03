package com.entri.tickets.mapper;

import com.entri.tickets.dto.TicketResponse;
import com.entri.tickets.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "reservationExternalId", source = "reservation.externalId")
    @Mapping(target = "eventExternalId", source = "event.externalId")
    @Mapping(target = "eventTitle", source = "event.title")
    @Mapping(target = "eventStartTime", source = "event.startTime")
    @Mapping(target = "eventEndTime", source = "event.endTime")
    @Mapping(target = "venueName", source = "event.venueName")
    @Mapping(target = "venueCity", source = "event.venueCity")
    @Mapping(target = "currency", source = "event.currency")
    @Mapping(target = "ticketTypeName", source = "ticketType.name")
    @Mapping(target = "ticketPrice", source = "ticketType.price")
    @Mapping(target = "holderFirstName", source = "reservation.firstName")
    @Mapping(target = "holderLastName", source = "reservation.lastName")
    TicketResponse toResponse(Ticket ticket);
}
