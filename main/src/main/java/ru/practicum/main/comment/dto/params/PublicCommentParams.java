package ru.practicum.main.comment.dto.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicCommentParams {

    private Long eventId;

    private int from;

    private int size;

    private String sort;
}
