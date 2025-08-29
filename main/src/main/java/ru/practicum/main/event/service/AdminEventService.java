package ru.practicum.main.event.service;

import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventUpdateDto;
import ru.practicum.main.event.dto.params.AdminEventParams;

import java.util.List;

public interface AdminEventService {

    List<EventFullDto> searchEvents(AdminEventParams params);

    EventFullDto updateEventByAdmin(Long eventId, EventUpdateDto updateDto);

}
