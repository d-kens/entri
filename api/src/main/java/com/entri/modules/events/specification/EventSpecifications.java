package com.entri.modules.events.specification;

import com.entri.modules.events.entity.Event;
import org.springframework.data.jpa.domain.Specification;

public class EventSpecifications {
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
}
