package ru.practicum.main.event.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.main.event.entity.Event;

import java.util.Optional;


@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    boolean existsByCategoryId(Long id);

    @EntityGraph(attributePaths = "publishedComments")
    Optional<Event> findWithPublishedCommentsById(Long id);

    @EntityGraph(attributePaths = "comments")
    Optional<Event> findWithAllCommentsById(Long id);

}
