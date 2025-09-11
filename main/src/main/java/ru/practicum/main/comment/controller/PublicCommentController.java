package ru.practicum.main.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.params.PublicCommentParams;
import ru.practicum.main.comment.service.PublicCommentService;

import java.util.List;

@RestController
@RequestMapping("/events/{eventId}/comments")
@RequiredArgsConstructor
@Slf4j
public class PublicCommentController {

    private final PublicCommentService service;

    @GetMapping
    public List<CommentDto> getCommentsByEvent(@PathVariable Long eventId,
                                               @RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(defaultValue = "DATE_DESC") String sort) {
        log.info("GET: Calling to endpoint /events/{}/comments from={}, size={}, sort={}", eventId, from, size, sort);
        PublicCommentParams params = PublicCommentParams.builder()
                .eventId(eventId)
                .from(from)
                .size(size)
                .sort(sort)
                .build();

        return service.getCommentsByEvent(params);
    }

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(@PathVariable Long eventId,
                                     @PathVariable Long commentId) {
        log.info("GET: Calling to endpoint /events/{}/comments/{}", eventId, commentId);
        return service.getCommentById(eventId, commentId);
    }
}
