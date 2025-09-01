package ru.practicum.main.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.entity.RequestStatus;
import ru.practicum.main.request.service.RequestService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
@Slf4j
public class RequestController {

    private final RequestService service;

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable Long userId,
                                                 @RequestParam Long eventId) {
        log.info("POST: Calling endpoint /users/{}/requests?eventId={}", userId, eventId);
        return service.createRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        log.warn("PATCH: Calling endpoint /users/{}/requests/{}/cancel", userId, requestId);
        return service.cancelRequest(userId, requestId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public ParticipationRequestDto moderateRequests(@PathVariable Long userId,
                                                    @PathVariable Long eventId,
                                                    @PathVariable Long requestId,
                                                    @RequestParam RequestStatus status) {
        return service.moderateRequest(userId, eventId, requestId, status);
    }
}
