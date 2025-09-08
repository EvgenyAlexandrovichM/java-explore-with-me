package ru.practicum.main.comment.service;

import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.NewCommentDto;
import ru.practicum.main.comment.dto.UpdateCommentDto;
import ru.practicum.main.comment.dto.params.PrivateCommentParams;

import java.util.List;

public interface PrivateCommentService {

    CommentDto createComment(Long eventId, Long userId, NewCommentDto dto);

    CommentDto updateComment(Long commentId, Long userId, UpdateCommentDto dto);

    void deleteComment(Long commentId, Long userId);

    List<CommentDto> getCommentsByEvent(PrivateCommentParams params);

    List<CommentDto> getUserComments(PrivateCommentParams params);

}
