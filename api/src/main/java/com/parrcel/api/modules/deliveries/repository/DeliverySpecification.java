package com.parrcel.api.modules.deliveries.repository;

import com.parrcel.api.modules.deliveries.entity.DeliveryStatus;
import com.parrcel.api.modules.deliveries.entity.Delivery;
import org.springframework.data.jpa.domain.Specification;

public class DeliverySpecification {

    public static Specification<Delivery> hasUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Delivery> hasStatus(DeliveryStatus status) {
        return (root, query, cb) -> cb.equal(root.get("deliveryStatus"), status);
    }

    public static Specification<Delivery> search(String searchTerm) {
        return (root, query, cb) -> {
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("trackingNumber")), pattern),
                    cb.like(cb.lower(root.get("recipientName")), pattern),
                    cb.like(cb.lower(root.get("recipientPhone")), pattern),
                    cb.like(cb.lower(root.get("packageName")), pattern)
            );
        };
    }
}