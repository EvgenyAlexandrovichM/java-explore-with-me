package ru.practicum.main.comment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.mapper.CommentMapper;
import ru.practicum.main.comment.dto.params.AdminCommentParams;
import ru.practicum.main.comment.entity.Comment;
import ru.practicum.main.comment.entity.CommentStatus;
import ru.practicum.main.comment.repository.CommentRepository;
import ru.practicum.main.comment.repository.specification.CommentSpecifications;
import ru.practicum.main.comment.service.AdminCommentService;
import ru.practicum.main.exception.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminCommentServiceImpl implements AdminCommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(AdminCommentParams params) {
        Pageable pageable = buildPageable(params);
        Specification<Comment> spec = CommentSpecifications.withFilters(params);

        Page<Comment> page = commentRepository.findAll(spec, pageable);

        return mapper.toDtoList(page.getContent());
    }

    @Override
    public CommentDto updateCommentStatus(Long commentId, CommentStatus status) {
        Comment comment = getCommentOrThrow(commentId);
        comment.setStatus(status);
        comment.setUpdated(LocalDateTime.now());

        Comment updated = commentRepository.save(comment);
        log.info("CommentId={} status updated to={}", updated.getId(), status);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getCommentById(Long commentId) {
        Comment comment = getCommentOrThrow(commentId);
        return mapper.toDto(comment);
    }

    @Override
    public void deleteComment(Long commentId) {
        Comment comment = getCommentOrThrow(commentId);
        commentRepository.delete(comment);
        log.info("CommentId={} deleted by admin", comment.getId());
    }

    private Comment getCommentOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("CommentId={} not found", id);
                    return new EntityNotFoundException("Comment with id " + id + " not found");
                });
    }

    private Pageable buildPageable(AdminCommentParams params) {
        return PageRequest.of(
                params.getFrom() / params.getSize(),
                params.getSize(),
                Sort.by(Sort.Direction.DESC, "created")
        );
    }
}
