package ru.practicum.main.comment.dto.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.comment.entity.CommentStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCommentParams {

    private Long eventId;

    private Long authorId;

    private CommentStatus status;

    private LocalDateTime rangeStart;

    private LocalDateTime rangeEnd;

    private int from;

    private int size;
}
