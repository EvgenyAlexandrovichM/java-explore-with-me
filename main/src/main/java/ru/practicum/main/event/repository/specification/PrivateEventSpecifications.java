package ru.practicum.main.event.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.main.event.entity.Event;
import ru.practicum.main.event.entity.EventState;

import java.util.List;

public class PrivateEventSpecifications extends EventSpecifications {

    public static Specification<Event> byInitiator(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("initiator").get("id"), userId);
        };
    }

    public static Specification<Event> byStates(List<EventState> states) {
        return (root, query, criteriaBuilder) -> {
            if (states == null || states.isEmpty()) return criteriaBuilder.conjunction();
            return root.get("state").in(states);
        };
    }
}
