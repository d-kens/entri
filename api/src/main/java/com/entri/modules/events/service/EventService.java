package com.entri.modules.events.service;

import com.entri.common.dto.PaginationResponse;
import com.entri.common.exception.ForbiddenException;
import com.entri.common.exception.NotFoundException;
import com.entri.modules.events.dto.EventRequest;
import com.entri.modules.events.dto.CreateTicketTypeRequest;
import com.entri.modules.events.dto.EventDetailResponse;
import com.entri.modules.events.dto.EventFilter;
import com.entri.modules.events.dto.EventResponse;
import com.entri.modules.events.entity.Event;
import com.entri.modules.events.entity.EventStatus;
import com.entri.modules.events.entity.TicketStatus;
import com.entri.modules.events.entity.TicketType;
import com.entri.modules.events.repository.EventRepository;
import com.entri.modules.events.service.mapper.EventMapper;
import com.entri.modules.events.specification.EventSpecifications;
import com.entri.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventMapper eventMapper;
    private final UserService userService;
    private final EventRepository eventRepository;
    private final EventCategoryService eventCategoryService;

    public PaginationResponse<EventResponse> getEvents(EventFilter filter) {
        Specification<Event> spec = buildSpecification(filter)
                .and(EventSpecifications.hasStatus(EventStatus.PUBLISHED))
                .and(EventSpecifications.isPublic());
        return fetchPage(filter, spec);
    }

    public PaginationResponse<EventResponse> getEventsForManagement(EventFilter filter, String organizerKey) {
        Specification<Event> spec = buildSpecification(filter);
        if (organizerKey != null) {
            spec = spec.and(EventSpecifications.hasOrganizer(organizerKey));
        }
        return fetchPage(filter, spec);
    }

    private PaginationResponse<EventResponse> fetchPage(EventFilter filter, Specification<Event> spec) {
        Sort sort = Sort.by(Sort.Direction.fromString(filter.sortDirection()), "startTime");
        Pageable pageable = PageRequest.of(filter.page(), filter.size(), sort);
        Page<Event> result = eventRepository.findAll(spec, pageable);
        List<EventResponse> content = result.getContent().stream()
                .map(eventMapper::toEventResponse)
                .toList();
        return new PaginationResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    @Transactional
    public EventResponse createEvent(EventRequest request, String currentUserKey) {
        var user = userService.findEntityByExternalKey(currentUserKey);
        var category = eventCategoryService.findById(request.categoryId());

        Event event = Event.builder()
                .organizer(user)
                .title(request.title())
                .description(request.description())
                .category(category)
                .venueName(request.venueName())
                .venueCity(request.venueCity())
                .venueCountry(request.venueCountry())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .bannerUrl(request.bannerUrl())
                .status(EventStatus.DRAFT)
                .isPublic(request.isPublic() == null || request.isPublic())
                .build();

        if (request.ticketTypes() != null && !request.ticketTypes().isEmpty()) {
            event.setTicketTypes(request.ticketTypes().stream()
                    .map(t -> toTicketType(t, event))
                    .toList());
        }

        eventRepository.save(event);
        return eventMapper.toEventResponse(event);
    }

    public EventDetailResponse getEventByExternalId(final String externalId) {
        var event = eventRepository.findByExternalId(externalId)
                .orElseThrow(() -> new NotFoundException("Event with external ID: " + externalId + " not found"));
        return eventMapper.toEventDetailResponse(event);
    }

    @Transactional
    public EventResponse updateEvent(
            final String externalId,
            final EventRequest request,
            final String currentUserKey,
            final boolean isPlatformAdmin
    ) {
        var event = eventRepository.findByExternalId(externalId)
                .orElseThrow(() -> new NotFoundException("Event with external ID: " + externalId + " not found"));

        boolean isEventOwner = event.getOrganizer().getExternalKey().equals(currentUserKey);

        if (!isEventOwner && !isPlatformAdmin) {
            throw new ForbiddenException("You are not authorized to update this event");
        }

        var category = eventCategoryService.findById(request.categoryId());

        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setCategory(category);
        event.setVenueName(request.venueName());
        event.setVenueCountry(request.venueCountry());
        event.setVenueCity(request.venueCity());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setBannerUrl(request.bannerUrl());
        if (request.isPublic() != null) {
            event.setPublic(request.isPublic());
        }

        eventRepository.save(event);
        return eventMapper.toEventResponse(event);
    }

    private Specification<Event> buildSpecification(EventFilter filter) {
        return Specification
                .where(EventSpecifications.hasCategory(filter.categoryId()))
                .and(EventSpecifications.search(filter.searchTerm()))
                .and(EventSpecifications.startFrom(filter.startFrom()))
                .and(EventSpecifications.startTo(filter.startTo()));
    }

    private TicketType toTicketType(CreateTicketTypeRequest t, Event event) {
        return TicketType.builder()
                .event(event)
                .name(t.name())
                .description(t.description())
                .price(t.price())
                .currency(t.currency())
                .quantity(t.quantity())
                .maxTicketsPerOrder(t.maxTicketsPerOrder())
                .saleStartDate(t.saleStartDate())
                .saleEndDate(t.saleEndDate())
                .status(TicketStatus.ACTIVE)
                .build();
    }
}
