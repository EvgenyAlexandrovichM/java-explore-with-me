package ru.practicum.main.request.service;

import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.entity.RequestStatus;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    ParticipationRequestDto moderateRequest(Long userId, Long eventId, Long requestId, RequestStatus status);
}
