package ru.practicum.main.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventUpdateDto;
import ru.practicum.main.event.dto.NewEventDto;
import ru.practicum.main.event.dto.params.PrivateEventParams;
import ru.practicum.main.event.entity.EventState;
import ru.practicum.main.event.service.PrivateEventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
public class PrivateEventController {

    private final PrivateEventService privateEventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId,
                                    @Valid @RequestBody NewEventDto dto) {
        log.info("POST: Calling to endpoint /users/{}/events - create event", userId);
        return privateEventService.createEvent(userId, dto);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateUserEvent(@PathVariable Long userId,
                                        @PathVariable Long eventId,
                                        @Valid @RequestBody EventUpdateDto dto) {
        log.info("PATCH: Calling to endpoint /users/{}/events/{} - update event", userId, eventId);
        return privateEventService.updateUserEvent(userId, eventId, dto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getUserEvent(@PathVariable Long userId,
                                     @PathVariable Long eventId,
                                     HttpServletRequest request) {
        log.info("GET: Calling to endpoint /users/{}/events/{} - get event", userId, eventId);
        return privateEventService.getUserEvent(userId, eventId, request);
    }

    @GetMapping
    public List<EventFullDto> getUserEvents(@PathVariable Long userId,
                                            @RequestParam(defaultValue = "0") int from,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) List<EventState> states) {

        PrivateEventParams params = PrivateEventParams.builder()
                .userId(userId)
                .from(from)
                .size(size)
                .states(states)
                .build();
        return privateEventService.getUserEvents(params);
    }

}
