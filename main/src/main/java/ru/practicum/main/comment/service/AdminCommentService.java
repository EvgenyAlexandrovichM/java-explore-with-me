package ru.practicum.main.comment.service;

import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.params.AdminCommentParams;
import ru.practicum.main.comment.entity.CommentStatus;

import java.util.List;

public interface AdminCommentService {

    List<CommentDto> getComments(AdminCommentParams params);

    CommentDto updateCommentStatus(Long commentId, CommentStatus status);

    CommentDto getCommentById(Long commentId);

    void deleteComment(Long commentId);
}
