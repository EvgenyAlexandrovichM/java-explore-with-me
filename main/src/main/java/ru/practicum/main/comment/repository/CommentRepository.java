package ru.practicum.main.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.main.comment.entity.Comment;
import ru.practicum.main.comment.entity.CommentStatus;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByEventIdAndStatus(Long eventId, CommentStatus status, Pageable pageable);

    Page<Comment> findAllByAuthorId(Long authorId, Pageable pageable);

    Page<Comment> findAllByEventIdAndAuthorId(Long eventId, Long authorId, Pageable pageable);

    Page<Comment> findAllByEventId(Long eventId, Pageable pageable);

    Page<Comment> findAllByStatus(CommentStatus status, Pageable pageable);
}
