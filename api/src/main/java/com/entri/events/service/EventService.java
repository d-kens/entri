package com.entri.events.service;

import com.entri.security.UserPrincipal;
import com.entri.exception.BadRequestException;
import com.entri.exception.UnauthorizedException;
import com.entri.common.dto.PaginationResponse;
import com.entri.exception.ResourceNotFoundException;
import com.entri.events.dto.EventFilter;
import com.entri.events.dto.EventRequest;
import com.entri.events.dto.EventResponse;
import com.entri.events.entity.Event;
import com.entri.events.entity.EventStatus;
import com.entri.events.repository.EventRepository;
import com.entri.events.mapper.EventMapper;
import com.entri.events.specification.EventSpecifications;
import com.entri.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventMapper eventMapper;
    private final UserService userService;
    private final EventRepository eventRepository;
    private final EventCategoryService eventCategoryService;

    public PaginationResponse<EventResponse> listEvents(EventFilter filter) {
        Specification<Event> statusSpec = EventSpecifications.hasStatus(EventStatus.PUBLISHED)
                .or(EventSpecifications.hasStatus(EventStatus.CANCELLED));

        Specification<Event> spec = buildSpecification(filter).and(statusSpec);
        return fetchPage(filter, spec);
    }

    public PaginationResponse<EventResponse> listOrganizerEvents(EventFilter filter, String organizerKey) {
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
        validateEventDates(request, true);
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
                .currency(request.currency())
                .status(EventStatus.DRAFT)
                .build();

        eventRepository.save(event);
        return eventMapper.toEventResponse(event);
    }

    public EventResponse getEventByExternalId(final String eventExternalId) {
        var event = eventRepository.findByExternalId(eventExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Event with external ID: " + eventExternalId + " not found"));
        return eventMapper.toEventResponse(event);
    }

    @Transactional
    public EventResponse publishEvent(final String eventExternalId, final UserPrincipal user) {
        var event = getAuthorizedEvent(eventExternalId, user);

        if (event.getTicketTypes().isEmpty()) {
            throw new BadRequestException(
                    "Event: " + eventExternalId + " cannot be published because it has no ticket types"
            );
        }

        if (event.getStatus() != EventStatus.DRAFT &&
                event.getStatus() != EventStatus.CANCELLED) {
            throw new BadRequestException(
                    "Event cannot be published because its status is not DRAFT or CANCELLED"
            );
        }

        if (event.getStartTime().isBefore(Instant.now())) {
            throw new BadRequestException(
                    "Event cannot be published because its start time is in the past"
            );
        }

        event.setStatus(EventStatus.PUBLISHED);
        event.setPublishedAt(Instant.now());
        return eventMapper.toEventResponse(event);
    }

    @Transactional
    public EventResponse cancelEvent(final String eventExternalId, final UserPrincipal user) {
        var event = getAuthorizedEvent(eventExternalId, user);

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BadRequestException(
                    "Event cannot be cancelled because its status is not PUBLISHED"
            );
        }
        event.setStatus(EventStatus.CANCELLED);
        return eventMapper.toEventResponse(event);
    }

    @Transactional
    public EventResponse updateEvent(
            final String externalId,
            final EventRequest request,
            final UserPrincipal user
    ) {
        var event = getAuthorizedEvent(externalId, user);

        validateEventDates(request, false);
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
        event.setCurrency(request.currency());

        eventRepository.save(event);
        return eventMapper.toEventResponse(event);
    }

    private Event getAuthorizedEvent(String externalId, UserPrincipal user) {
        var event = eventRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Event with external ID: " + externalId + " not found"));
        assertCanManage(event, user);
        return event;
    }

    private void assertCanManage(Event event, UserPrincipal user) {
        if (!user.isAdmin() && !user.getExternalKey().equals(event.getOrganizer().getExternalKey())) {
            throw new UnauthorizedException("You are not authorized to perform this action");
        }
    }

    private void validateEventDates(EventRequest request, boolean isCreate) {
        if (isCreate && request.startTime().isBefore(Instant.now())) {
            throw new BadRequestException("Start time must not be in the past");
        }
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("End time must be after start time");
        }
    }

    private Specification<Event> buildSpecification(EventFilter filter) {
        return Specification
                .where(EventSpecifications.hasCategory(filter.categoryId()))
                .and(EventSpecifications.search(filter.searchTerm()))
                .and(EventSpecifications.startFrom(filter.startFrom()))
                .and(EventSpecifications.startTo(filter.startTo()));
    }
}
