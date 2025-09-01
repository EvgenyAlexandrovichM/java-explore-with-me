package ru.practicum.main.event.dto.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.event.entity.EventState;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivateEventParams {
    private Long userId;
    private List<EventState> states;
    private int from;
    private int size;
}
