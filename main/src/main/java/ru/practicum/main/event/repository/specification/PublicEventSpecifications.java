package ru.practicum.main.event.repository.specification;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.main.event.entity.Event;
import ru.practicum.main.event.entity.EventState;
import ru.practicum.main.request.entity.Request;
import ru.practicum.main.request.entity.RequestStatus;

public class PublicEventSpecifications extends EventSpecifications {

    public static Specification<Event> isPublished() {
        return (root, query, cb) ->
                cb.equal(root.get("state"), EventState.PUBLISHED);
    }

    public static Specification<Event> onlyAvailable(Boolean onlyAvailable) {
        return (root, query, criteriaBuilder) -> {
            if (onlyAvailable == null || !onlyAvailable) return criteriaBuilder.conjunction();

            Subquery<Long> confirmedCountSubquery = query.subquery(Long.class);
            Root<Request> requestRoot = confirmedCountSubquery.from(Request.class);
            confirmedCountSubquery.select(criteriaBuilder.count(requestRoot));
            confirmedCountSubquery.where(
                    criteriaBuilder.equal(requestRoot.get("event"), root),
                    criteriaBuilder.equal(requestRoot.get("status"), RequestStatus.CONFIRMED)
            );

            return criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("participantLimit"), 0),
                    criteriaBuilder.lessThan(confirmedCountSubquery, root.get("participantLimit"))
            );
        };
    }
}
