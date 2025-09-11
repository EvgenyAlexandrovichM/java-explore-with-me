package ru.practicum.main.comment.service;

import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.params.PublicCommentParams;

import java.util.List;

public interface PublicCommentService {

    List<CommentDto> getCommentsByEvent(PublicCommentParams params);

    CommentDto getCommentById(Long eventId, Long commentId);
}
