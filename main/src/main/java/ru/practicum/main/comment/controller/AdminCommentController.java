package ru.practicum.main.comment.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.params.AdminCommentParams;
import ru.practicum.main.comment.entity.CommentStatus;
import ru.practicum.main.comment.service.AdminCommentService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Slf4j
public class AdminCommentController {

    private final AdminCommentService service;

    @GetMapping
    public List<CommentDto> getComments(@RequestParam(required = false) Long eventId,
                                        @RequestParam(required = false) Long authorId,
                                        @RequestParam(required = false)CommentStatus status,
                                        @RequestParam(required = false)
                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                        @RequestParam(required = false)
                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                        @RequestParam(defaultValue = "0") @Min(0) int from,
                                        @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info(
                "GET: /admin/comments eventId={}, authorId={}, status={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                eventId, authorId, status, rangeStart, rangeEnd, from, size
        );

        AdminCommentParams params = AdminCommentParams.builder()
                .eventId(eventId)
                .authorId(authorId)
                .status(status)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();

        return service.getComments(params);
    }

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(@PathVariable Long commentId) {
        log.info("GET: /admin/comments/{}", commentId);
        return service.getCommentById(commentId);
    }

    @PatchMapping("/{commentId}/status")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateCommentStatus(@PathVariable Long commentId,
                                          @RequestParam CommentStatus status) {
        log.info("PATCH: /admin/comments/{}/status={}", commentId, status);
        return service.updateCommentStatus(commentId, status);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        log.info("DELETE: /admin/comments/{}", commentId);
        service.deleteComment(commentId);
    }
}
