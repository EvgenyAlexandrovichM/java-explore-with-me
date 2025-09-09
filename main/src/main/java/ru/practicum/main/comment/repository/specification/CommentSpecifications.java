package ru.practicum.main.comment.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.main.comment.dto.params.AdminCommentParams;
import ru.practicum.main.comment.entity.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentSpecifications {

    public static Specification<Comment> withFilters(AdminCommentParams params) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.getEventId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("event").get("id"), params.getEventId()));
            }

            if (params.getAuthorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("author").get("id"), params.getAuthorId()));
            }

            if (params.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), params.getStatus()));
            }

            if (params.getRangeStart() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("created"), params.getRangeStart()));
            }

            if (params.getRangeEnd() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("created"), params.getRangeEnd()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
