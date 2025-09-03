package ru.practicum.main.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.entity.RequestStatus;
import ru.practicum.main.request.service.RequestService;

import java.util.List;

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
    public EventRequestStatusUpdateResult moderateRequests(@PathVariable Long userId,
                                                           @PathVariable Long eventId,
                                                           @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        log.info("PATCH: Calling endpoint /users/{}/events/{}/requests", updateRequest, eventId);
        return service.moderateRequest(userId, eventId, updateRequest);
    }

    @GetMapping("/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        log.info("GET: Calling endpoint /users/{}/requests", userId);
        return service.getUserRequests(userId);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId,
                                                          @PathVariable Long eventId) {
        log.info("GET: Calling endpoint /users/{}/events/{}/requests", userId, eventId);
        return service.getEventRequests(userId, eventId);
    }
}
