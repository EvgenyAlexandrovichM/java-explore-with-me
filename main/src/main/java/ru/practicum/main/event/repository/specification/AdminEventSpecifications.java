package ru.practicum.main.event.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.main.event.entity.Event;
import ru.practicum.main.event.entity.EventState;

import java.util.List;


public class AdminEventSpecifications extends EventSpecifications {

    public static Specification<Event> byUsers(List<Long> userIds) {
        return (root, query, criteriaBuilder) -> {
            if (userIds == null || userIds.isEmpty()) return criteriaBuilder.conjunction();
            return root.get("initiator").get("id").in(userIds);
        };
    }

    public static Specification<Event> byStates(List<EventState> states) {
        return (root, query, criteriaBuilder) -> {
            if (states == null || states.isEmpty()) return criteriaBuilder.conjunction();
            return root.get("state").in(states);
        };
    }
}
