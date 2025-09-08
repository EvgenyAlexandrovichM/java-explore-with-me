package ru.practicum.main.comment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.NewCommentDto;
import ru.practicum.main.comment.dto.UpdateCommentDto;
import ru.practicum.main.comment.dto.mapper.CommentMapper;
import ru.practicum.main.comment.dto.params.PrivateCommentParams;
import ru.practicum.main.comment.entity.Comment;
import ru.practicum.main.comment.entity.CommentStatus;
import ru.practicum.main.comment.repository.CommentRepository;
import ru.practicum.main.comment.service.PrivateCommentService;
import ru.practicum.main.event.entity.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.EntityNotFoundException;
import ru.practicum.main.user.entity.User;
import ru.practicum.main.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PrivateCommentServiceImpl implements PrivateCommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentMapper mapper;

    private static final long EDIT_TIME_LIMIT_MINUTES = 15;

    @Override
    public CommentDto createComment(Long eventId, Long userId, NewCommentDto dto) {
        Event event = getEventOrThrow(eventId);
        User author = getUserOrThrow(userId);

        Comment comment = prepareNewComment(dto, event, author);
        Comment saved = commentRepository.save(comment);

        log.info("CommentId={} saved", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    public CommentDto updateComment(Long commentId, Long userId, UpdateCommentDto dto) {
        Comment comment = getCommentOrThrow(commentId);

        validateAuthor(comment, userId);
        validateEditTime(comment);

        comment.setText(dto.getText());
        comment.setUpdated(LocalDateTime.now());

        Comment updated = commentRepository.save(comment);
        log.info("CommentId={} updated", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = getCommentOrThrow(commentId);

        validateAuthor(comment, userId);
        commentRepository.delete(comment);
        log.info("CommentId={} deleted", comment.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByEvent(PrivateCommentParams params) {
        Pageable pageable = buildPageable(params);

        return mapper.toDtoList(
                commentRepository.findAllByEventIdAndAuthorId(
                        params.getEventId(),
                        params.getUserId(),
                        pageable
                ).getContent()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getUserComments(PrivateCommentParams params) {
        Pageable pageable = buildPageable(params);
        return mapper.toDtoList(
                commentRepository.findAllByAuthorId(params.getUserId(), pageable).getContent()
        );
    }

    private Comment prepareNewComment(NewCommentDto dto, Event event, User author) {
        Comment comment = mapper.toEntity(dto);
        comment.setEvent(event);
        comment.setAuthor(author);
        comment.setStatus(CommentStatus.PUBLISHED);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    private void validateAuthor(Comment comment, Long userId) {
        if (!comment.getAuthor().getId().equals(userId)) {
            log.warn("UserId={} doesn't match the authorId={}", userId, comment.getAuthor());
            throw new ConflictException("Only author can perform this action");
        }
    }

    private void validateEditTime(Comment comment) {
        long minutesSinceCreation = ChronoUnit.MICROS.between(comment.getCreated(), LocalDateTime.now());
        if (minutesSinceCreation > EDIT_TIME_LIMIT_MINUTES) {
            log.warn("Edit time exceeded for commentId={}", comment.getId());
            throw new ConflictException("Comment updating is available for 15 minutes");
        }
    }

    private Pageable buildPageable(PrivateCommentParams params) {
        return PageRequest.of(
                params.getFrom() / params.getSize(),
                params.getSize(),
                Sort.by(Sort.Direction.DESC, "created")
        );
    }


    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("EventId={} not found", id);
                    return new EntityNotFoundException("Event with id " + id + " not found");
                });
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("UserId={} not found", id);
                    return new EntityNotFoundException("User with id " + id + " not found");
                });
    }

    private Comment getCommentOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("CommentId={} not found", id);
                    return new EntityNotFoundException("Comment with id " + id + " not found");
                });
    }
}
