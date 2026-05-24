package com.api.modules.events.service;

import com.api.common.exception.FileUploadException;
import com.api.common.storage.FirebaseStorageService;
import com.api.modules.events.dto.CreateEventRequest;
import com.api.modules.events.dto.CreateTicketTypeRequest;
import com.api.modules.events.dto.EventResponse;
import com.api.modules.events.entity.Event;
import com.api.modules.events.entity.EventStatus;
import com.api.modules.events.entity.TicketStatus;
import com.api.modules.events.entity.TicketType;
import com.api.modules.events.repository.EventRepository;
import com.api.modules.events.service.mapper.EventMapper;
import com.api.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventMapper eventMapper;
    private final UserService userService;
    private final FirebaseStorageService firebaseStorageService;

    private final EventRepository eventRepository;
    private final EventCategoryService eventCategoryService;

    @Transactional
    public EventResponse createEvent(CreateEventRequest request, String currentUserKey) {
        var user = userService.findEntityByExternalKey(currentUserKey);
        var category = eventCategoryService.findById(request.categoryId());
        var bannerUrl = uploadBanner(request);

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
                .bannerUrl(bannerUrl)
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

    private String uploadBanner(CreateEventRequest request) {
        try {
            return firebaseStorageService.upload(request.bannerImage());
        } catch (IOException e) {
            throw new FileUploadException("Image upload failed", e);
        }
    }

    private TicketType toTicketType(CreateTicketTypeRequest t, Event event) {
        return TicketType.builder()
                .event(event)
                .name(t.name())
                .description(t.description())
                .price(t.price())
                .currency(t.currency())
                .quantity(t.quantity())
                .maxPerOrder(t.maxPerOrder())
                .saleStartDate(t.saleStartDate())
                .saleEndDate(t.saleEndDate())
                .displayOrder(t.displayOrder() != null ? t.displayOrder() : 0)
                .isHidden(t.isHidden() != null && t.isHidden())
                .status(TicketStatus.ACTIVE)
                .build();
    }
}
