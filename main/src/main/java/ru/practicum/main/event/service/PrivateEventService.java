package ru.practicum.main.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventUpdateDto;
import ru.practicum.main.event.dto.NewEventDto;
import ru.practicum.main.event.dto.params.PrivateEventParams;

import java.util.List;

public interface PrivateEventService {

    EventFullDto createEvent(Long userId, NewEventDto dto);

    EventFullDto updateUserEvent(Long userId, Long eventId, EventUpdateDto dto);

    EventFullDto getUserEvent(Long userId, Long eventId, HttpServletRequest request);

    List<EventFullDto> getUserEvents(PrivateEventParams params);

}
