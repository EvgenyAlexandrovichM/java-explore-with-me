package ru.practicum.main.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.event.entity.Event;
import ru.practicum.main.event.entity.EventState;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.EntityAlreadyExistsException;
import ru.practicum.main.exception.EntityNotFoundException;
import ru.practicum.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.dto.mapper.RequestMapper;
import ru.practicum.main.request.entity.Request;
import ru.practicum.main.request.entity.RequestStatus;
import ru.practicum.main.request.repository.RequestRepository;
import ru.practicum.main.user.entity.User;
import ru.practicum.main.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper mapper;

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("Create request: userId={}, eventId={}", userId, eventId);

        User user = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);
        validateNewRequest(user, event);

        boolean moderation = event.getRequestModeration() == null || event.getRequestModeration();
        RequestStatus status = (event.getParticipantLimit() == 0 || !moderation
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING);

        Request request = Request.builder()
                .event(event)
                .requester(user)
                .created(LocalDateTime.now())
                .status(status)
                .build();

        log.info("Request with id={} saved", request.getId());

        return mapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Getting userId={} requests", userId);
        return mapper.toDtoList(requestRepository.findAllByRequesterId(userId));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Cancel requestId={} by userId={}", requestId, userId);
        Request request = getRequestOrThrow(requestId);
        checkRequester(request, userId);

        request.setStatus(RequestStatus.CANCELED);
        log.info("RequestId={} canceled", requestId);

        return mapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Getting requests for eventId={} by initiator={}", eventId, userId);

        Event event = getEventOrThrow(eventId);
        checkInitiator(event, userId);

        return mapper.toDtoList(requestRepository.findAllByEventId(eventId));
    }

    @Override
    public EventRequestStatusUpdateResult moderateRequest(Long userId,
                                                          Long eventId,
                                                          EventRequestStatusUpdateRequest updateRequest) {
        log.info("Moderating requests for eventId={} by userId={}, targetStatus={}, requestIds={}",
                eventId, userId, updateRequest.getStatus(), updateRequest.getRequestIds());
        Event event = getEventOrThrow(eventId);
        checkInitiator(event, userId);

        List<Request> requests = getRequestsForEventOrThrow(eventId, updateRequest.getRequestIds());
        List<Request> updatedRequests = updateRequestsStatus(event, requests, updateRequest.getStatus());

        requestRepository.saveAll(updatedRequests);

        return buildUpdateResult(updatedRequests);
    }

    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("EventId={} not found", id);
                    return new EntityNotFoundException("Event with id " + id + " not found");
                });
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("UserId={} not found", id);
                    return new EntityNotFoundException("User with id " + id + " not found");
                });
    }

    private Request getRequestOrThrow(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("RequestId={} not found", id);
                    return new EntityNotFoundException("Request with id " + id + " not found");
                });
    }

    private List<Request> getRequestsForEventOrThrow(Long eventId, List<Long> requestIds) {
        return requestRepository.findAllById(requestIds).stream()
                .peek(req -> validateRequestBelongsToEvent(req, eventId))
                .toList();
    }

    private void validateRequestBelongsToEvent(Request request, Long eventId) {
        if (!request.getEvent().getId().equals(eventId)) {
            log.warn("Request={} doesn't belong to event={}", request.getId(), eventId);
            throw new ConflictException("Request " + request.getId() + " doesn't belong to event " + eventId);
        }
    }

    private List<Request> updateRequestsStatus(Event event, List<Request> requests, RequestStatus newStatus) {
       if (newStatus == RequestStatus.CONFIRMED) {
           requests.forEach(req -> {
               checkParticipantLimit(event);
               req.setStatus(RequestStatus.CONFIRMED);
               log.trace("RequestId={} set to CONFIRMED", req.getId());
           });
       } else if (newStatus == RequestStatus.REJECTED) {
           requests.forEach(req -> {
               req.setStatus(RequestStatus.REJECTED);
               log.trace("RequestId={} set to REJECTED", req.getId());
           });
       }
       return requests;
    }

    private EventRequestStatusUpdateResult buildUpdateResult(List<Request> requests) {
        List<ParticipationRequestDto> confirmed = mapper.toDtoList(
                requests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.CONFIRMED)
                        .toList()
        );

        List<ParticipationRequestDto> rejected = mapper.toDtoList(
                requests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.REJECTED)
                        .toList()
        );
        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }

    private void checkInitiator(Event event, Long userId) {
        if (!event.getInitiator().getId().equals(userId)) {
            log.warn("UserId={} is not the initiator of the eventId={}", userId, event.getId());
            throw new ConflictException("Only initiator can perform this action");
        }
    }

    private void checkRequester(Request request, Long userId) {
        if (!request.getRequester().getId().equals(userId)) {
            log.warn("UserId={} cannot modify another user requestId={}", userId, request.getId());
            throw new ConflictException("Cannot modify another user requests");
        }
    }

    private void validateNewRequest(User user, Event event) {
        if (event.getInitiator().getId().equals(user.getId())) {
            log.warn("UserId={} cannot request own eventId={}", user.getId(), event.getId());
            throw new ConflictException("Cannot request own event");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            log.warn("EventId={} is not published", event.getId());
            throw new ConflictException("Event is not published");
        }

        if (requestRepository.findByEventIdAndRequesterId(event.getId(), user.getId()).isPresent()) {
            log.warn("Request on eventId={} already exists", event.getId());
            throw new EntityAlreadyExistsException("Request already exists");
        }

        checkParticipantLimit(event);
    }

    private void checkParticipantLimit(Event event) {
        if (event.getParticipantLimit() > 0 &&
                requestRepository.countByEventIdAndStatus(
                        event.getId(), RequestStatus.CONFIRMED) >= event.getParticipantLimit()) {
            log.warn("Participant limit={} reached for eventId={}", event.getParticipantLimit(), event.getId());
            throw new ConflictException("Participant limit reached");
        }
    }

}
