package ru.practicum.main.comment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.mapper.CommentMapper;
import ru.practicum.main.comment.dto.params.PublicCommentParams;
import ru.practicum.main.comment.entity.Comment;
import ru.practicum.main.comment.entity.CommentStatus;
import ru.practicum.main.comment.repository.CommentRepository;
import ru.practicum.main.comment.service.PublicCommentService;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.EntityNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PublicCommentServiceImpl implements PublicCommentService {

    private final CommentRepository repository;
    private final CommentMapper mapper;

    @Override
    public List<CommentDto> getCommentsByEvent(PublicCommentParams params) {
        validateParams(params);
        List<Comment> comments = findComments(params);
        return mapper.toDtoList(comments);
    }

    @Override
    public CommentDto getCommentById(Long eventId, Long commentId) {
        Comment comment = getCommentOrThrow(commentId);

        if (!comment.getEvent().getId().equals(eventId)) {
            log.warn("CommentId={} doesn't apply eventId={}", commentId, eventId);
            throw new ConflictException("Comment " + commentId + " doesn't apply to event " + eventId);
        }

        if (comment.getStatus() != CommentStatus.PUBLISHED) {
            log.warn("CommentId={} not published", comment.getId());
            throw new ConflictException("Comment " + commentId + " not published");
        }

        return mapper.toDto(comment);
    }

    private Comment getCommentOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("CommentId={} not found", id);
                    return new EntityNotFoundException("Comment with id " + id + " not found");
                });
    }

    private List<Comment> findComments(PublicCommentParams params) {
        Pageable pageable = PageRequest.of(
                params.getFrom() / params.getSize(),
                params.getSize(),
                resolveSort(params.getSort()));
        return repository.findAllByEventIdAndStatus(
                params.getEventId(),
                CommentStatus.PUBLISHED,
                pageable
        ).getContent();
    }

    private Sort resolveSort(String sort) {
        if ("DATE_ASC".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.ASC, "created");
        }
        return Sort.by(Sort.Direction.DESC, "created");
    }

    private void validateParams(PublicCommentParams params) {
        if (params.getEventId() == null) {
            log.warn("Invalid params: eventId is null");
            throw new BadRequestException("Event id must be provided");
        }
    }
}
