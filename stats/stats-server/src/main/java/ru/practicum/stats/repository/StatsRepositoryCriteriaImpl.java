package ru.practicum.stats.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.entity.EndpointHit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StatsRepositoryCriteriaImpl implements StatsRepositoryCriteria {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ViewStats> findStatsByCriteria(LocalDateTime start,
                                               LocalDateTime end,
                                               List<String> uris,
                                               boolean unique) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ViewStats> dataQuery = cb.createQuery(ViewStats.class);

        Root<EndpointHit> root = dataQuery.from(EndpointHit.class);
        Expression<Long> hitCount = unique ? cb.countDistinct(root.get("ip")) : cb.count(root.get("id"));

        dataQuery.select(cb.construct(
                ViewStats.class,
                root.get("app"),
                root.get("uri"),
                hitCount
        ));

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.between(root.get("timestamp"), start, end));

        if (uris != null && !uris.isEmpty()) {
            predicates.add(root.get("uri").in(uris));
        }
        dataQuery.where(predicates.toArray(new Predicate[0]));
        dataQuery.groupBy(root.get("app"), root.get("uri"));
        dataQuery.orderBy(cb.desc(hitCount));

        return em.createQuery(dataQuery).getResultList();
    }
}
