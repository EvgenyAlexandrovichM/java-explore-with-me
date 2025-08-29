package ru.practicum.main.event.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.main.event.entity.Event;

import java.time.LocalDateTime;
import java.util.List;

public class EventSpecifications {

    public static Specification<Event> hasText(String text) {
        return (root, query, criteriaBuilder) -> {
            if (text == null || text.isBlank()) return criteriaBuilder.conjunction();
            String pattern = "%" + text.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Event> inCategories(List<Long> categories) {
        return (root, query, criteriaBuilder) -> {
            if (categories == null || categories.isEmpty()) return criteriaBuilder.conjunction();
            return root.get("category").get("id").in(categories);
        };
    }

    public static Specification<Event> isPaid(Boolean paid) {
        return (root, query, criteriaBuilder) -> {
            if (paid == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("paid"), paid);
        };
    }

    public static Specification<Event> startAfter(LocalDateTime start) {
        return (root, query, criteriaBuilder) -> {
            if (start == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), start);
        };
    }

    public static Specification<Event> endBefore(LocalDateTime end) {
        return (root, query, criteriaBuilder) -> {
            if (end == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), end);
        };
    }
}

