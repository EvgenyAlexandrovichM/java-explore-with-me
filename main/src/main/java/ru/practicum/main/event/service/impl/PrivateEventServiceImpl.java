package ru.practicum.main.event.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.entity.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventUpdateDto;
import ru.practicum.main.event.dto.NewEventDto;
import ru.practicum.main.event.dto.StateAction;
import ru.practicum.main.event.dto.mapper.EventMapper;
import ru.practicum.main.event.dto.params.PrivateEventParams;
import ru.practicum.main.event.entity.Event;
import ru.practicum.main.event.entity.EventState;
import ru.practicum.main.event.entity.location.Location;
import ru.practicum.main.event.dto.location.LocationDto;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.event.repository.specification.PrivateEventSpecifications;
import ru.practicum.main.event.service.PrivateEventService;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.EntityNotFoundException;
import ru.practicum.main.request.entity.RequestStatus;
import ru.practicum.main.stats.hit.HitLoggingService;
import ru.practicum.main.stats.view.EventViewService;
import ru.practicum.main.user.entity.User;
import ru.practicum.main.user.repository.UserRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PrivateEventServiceImpl implements PrivateEventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper mapper;
    private final UserRepository userRepository;
    private final HitLoggingService hitLoggingService;
    private final EventViewService eventViewService;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        validateEventDate(dto.getEventDate());

        Event event = mapper.fromNewEventDto(dto);
        event.setInitiator(getUserOrThrow(userId));
        event.setCategory(getCategoryOrThrow(dto.getCategory()));
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setParticipantLimit(dto.getParticipantLimit() == null ? 0L : dto.getParticipantLimit());
        event.setRequestModeration(dto.getRequestModeration() == null ? true : dto.getRequestModeration());
        log.info("Event with id={} created", event.getId());
        return mapper.toFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, EventUpdateDto dto) {
        Event event = getEventOrThrow(eventId);

        checkOwnership(event, userId);

        validateUpdatableState(event);

        if (dto.getEventDate() != null) {
            validateEventDate(dto.getEventDate());
            event.setEventDate(dto.getEventDate());
        }

        updateBasicFields(event, dto);
        log.info("Basic fields updated for event id={}", event.getId());
        updateCategoryIfNeeded(event, dto.getCategoryId());
        log.info("Category={} updated for event id={}", event.getCategory(), event.getId());
        updateLocationIfNeeded(event, dto.getLocation());
        log.info("Location={} updated for event id={}", event.getLocation(), event.getId());
        updateStateIfNeeded(event, dto.getStateAction());
        log.info("State={} updated for event id={}", event.getState(), event.getId());

        log.info("Event with id={} updated", event.getId());
        return mapper.toFullDto(eventRepository.save(event));

    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId, HttpServletRequest request) {
        getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);
        checkOwnership(event, userId);

        logRequest(request);

        EventFullDto dto = mapper.toFullDto(event);
        dto.setViews(getViewsForSingleEvent(event));
        return dto;
    }

    @Override
    public List<EventFullDto> getUserEvents(PrivateEventParams params) {
        getUserOrThrow(params.getUserId());
        Pageable pageable = buildPageable(params);
        Specification<Event> spec = buildSpecification(params.getUserId(), params.getStates());

        Page<Event> evetsPage = eventRepository.findAll(spec, pageable);

        return mapToFullDtoWithViews(evetsPage.getContent());
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

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("CategoryId={} not found", id);
                    return new EntityNotFoundException("Category with id " + id + " not found");
                });
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate == null || eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            log.warn("Event date must be at least 2 hours from now, but={}", eventDate);
            throw new ConflictException("Event date must be at least 2 hours from " + LocalDateTime.now());
        }
    }

    private void validateUpdatableState(Event event) {
        if (event.getState() == EventState.PUBLISHED) {
            log.warn("Event state={}, cannot update", event.getState());
            throw new ConflictException("Cannot update published event");
        }
    }

    private void updateBasicFields(Event event, EventUpdateDto dto) {
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
    }

    private void updateStateIfNeeded(Event event, StateAction stateAction) {
        if (stateAction != null) {
            if (stateAction.equals(StateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            }
            if (stateAction.equals(StateAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            }
        }
    }

    private void updateCategoryIfNeeded(Event event, Long categoryId) {
        if (categoryId != null) {
            event.setCategory(getCategoryOrThrow(categoryId));
        }
    }

    private void updateLocationIfNeeded(Event event, LocationDto locationDto) {
        if (locationDto != null) {
            Location location = new Location();
            location.setLat(locationDto.getLat());
            location.setLon(locationDto.getLon());
            event.setLocation(location);
        }
    }

    private void checkOwnership(Event event, Long userId) {
        if (!event.getInitiator().getId().equals(userId)) {
            log.warn("Event id={} not available for user id={}", event.getId(), userId);
            throw new ConflictException("Can manage only own events");
        }
    }

    private void logRequest(HttpServletRequest request) {
        hitLoggingService.logRequest(request.getRequestURI(), request.getRemoteAddr());
    }

    private Specification<Event> buildSpecification(Long userId, List<EventState> states) {
        return Specification
                .where(PrivateEventSpecifications.byInitiator(userId))
                .and(PrivateEventSpecifications.byStates(states));
    }

    private Pageable buildPageable(PrivateEventParams params) {
        return PageRequest.of(params.getFrom() / params.getSize(),
                params.getSize(), Sort.by("eventDate").descending());
    }

    private long getViewsForSingleEvent(Event event) {
        return eventViewService.getViewsForEvents(List.of(event))
                .getOrDefault(event.getId(), 0L);
    }

    private List<EventFullDto> mapToFullDtoWithViews(List<Event> events) {
        Map<Long, Long> views = eventViewService.getViewsForEvents(events);
        return events.stream()
                .map(e -> enrichEventDto(e, views))
                .toList();
    }

    private EventFullDto enrichEventDto(Event event, Map<Long, Long> views) {
        EventFullDto dto = mapper.toFullDto(event);
        dto.setViews(views.getOrDefault(event.getId(), 0L));
        dto.setConfirmedRequests(
                event.getRequests() == null ? 0L :
                        event.getRequests().stream()
                                .filter(r -> r.getStatus() == RequestStatus.CONFIRMED)
                                .count()
        );
        return dto;
    }
}
