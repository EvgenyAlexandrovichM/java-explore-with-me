package ru.practicum.main.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.NewCommentDto;
import ru.practicum.main.comment.dto.UpdateCommentDto;
import ru.practicum.main.comment.dto.params.PrivateCommentParams;
import ru.practicum.main.comment.service.PrivateCommentService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Slf4j
public class PrivateCommentController {

    private final PrivateCommentService service;

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @Valid @RequestBody NewCommentDto dto) {
        log.info("POST: Calling to endpoint /users/{}/comments/events/{} body={}", userId, eventId, dto);
        return service.createComment(eventId, userId, dto);
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@PathVariable Long userId,
                                    @PathVariable Long commentId,
                                    @Valid @RequestBody UpdateCommentDto dto) {
        log.info("PATCH: Calling to endpoint /users/{}/comments/{} body={}", userId, commentId, dto);
        return service.updateComment(commentId, userId, dto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        log.info("DELETE: Calling to endpoint /users/{}/comments/{}", userId, commentId);
        service.deleteComment(commentId, userId);
    }

    @GetMapping
    private List<CommentDto> getUserComments(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "0") @Min(0) int from,
                                             @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("GET: Calling to endpoint /users/{}/comments from={}/ size={}", userId, from, size);
        PrivateCommentParams params = PrivateCommentParams.builder()
                .userId(userId)
                .from(from)
                .size(size)
                .build();
        return service.getUserComments(params);
    }

    @GetMapping("/events/{eventId}")
    public List<CommentDto> getCommentsByEvent(@PathVariable Long userId,
                                               @PathVariable Long eventId,
                                               @RequestParam(defaultValue = "0") @Min(0) int from,
                                               @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("GET: Calling to endpoint /users/{}/comments/events/{} from={}, size={}", userId, eventId, from, size);
        PrivateCommentParams params = PrivateCommentParams.builder()
                .userId(userId)
                .eventId(eventId)
                .from(from)
                .size(size)
                .build();
        return service.getCommentsByEvent(params);
    }

}
