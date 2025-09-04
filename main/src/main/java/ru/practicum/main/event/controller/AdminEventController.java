package ru.practicum.main.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventUpdateDto;
import ru.practicum.main.event.dto.params.AdminEventParams;
import ru.practicum.main.event.entity.EventState;
import ru.practicum.main.event.service.AdminEventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
public class AdminEventController {

    private final AdminEventService service;

    @GetMapping
    public List<EventFullDto> searchEvents(
            @RequestParam(required = false) List<Long> userIds,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        AdminEventParams params = AdminEventParams.builder()
                .userIds(userIds)
                .states(states)
                .categoryIds(categoryIds)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();
        log.info("Calling to endpoint /admin/events with params={}", params.toString());
        return service.searchEvents(params);
    }

    @PatchMapping("/{eventId}")
    EventFullDto updateEventByAdmin(@PathVariable Long eventId,
                                    @RequestBody @Valid EventUpdateDto dto) {
        log.info("Updating event id={} by admin with data={}", eventId, dto);
        return service.updateEventByAdmin(eventId, dto);
    }

}
