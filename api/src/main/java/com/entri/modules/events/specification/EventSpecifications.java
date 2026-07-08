package com.entri.modules.events.specification;

import com.entri.modules.events.entity.Event;
import com.entri.modules.events.entity.EventStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class EventSpecifications {
    public static Specification<Event> hasStatus(EventStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Event> hasCategory(Long categoryId) {
        return (root, query, cb) ->
                categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Event> search(String searchTerm) {
        return (root, query, cb) -> {

            if (searchTerm == null) return null;

            String pattern = "%" + searchTerm.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Event> hasOrganizer(String organizerExternalId) {
        return (root, query, cb) ->
                organizerExternalId == null
                        ? null
                        : cb.equal(root.get("organizer").get("externalKey"), organizerExternalId);
    }

    public static Specification<Event> startFrom(String startFromStr) {
        if (startFromStr == null) return (root, query, cb) -> null;
        Instant from = Instant.parse(startFromStr);
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), from);
    }

    public static Specification<Event> startTo(String startToStr) {
        if (startToStr == null) return (root, query, cb) -> null;
        Instant to = Instant.parse(startToStr);
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startTime"), to);
    }
}
