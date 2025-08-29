package ru.practicum.main.event.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.entity.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventUpdateDto;
import ru.practicum.main.event.dto.NewEventDto;
import ru.practicum.main.event.dto.mapper.EventMapper;
import ru.practicum.main.event.dto.params.PrivateEventParams;
import ru.practicum.main.event.entity.Event;
import ru.practicum.main.event.entity.EventState;
import ru.practicum.main.event.location.Location;
import ru.practicum.main.event.location.LocationDto;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.event.repository.specification.PrivateEventSpecifications;
import ru.practicum.main.event.service.PrivateEventService;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.EntityNotFoundException;
import ru.practicum.main.stats.hit.HitLoggingService;
import ru.practicum.main.user.entity.User;
import ru.practicum.main.user.repository.UserRepository;


import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        Event event = mapper.fromNewEventDto(dto);
        User initiator = getUserOrThrow(userId);
        initiator.setId(userId);
        event.setInitiator(initiator);

        Category category = getCategoryOrThrow(dto.getCategory());
        event.setCategory(category);

        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setViews
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, EventUpdateDto dto) {
        return null;
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId, HttpServletRequest request) {
        logRequest(request);
        Event event = getEventOrThrow(eventId);
        getUserOrThrow(userId);
        checkOwnership(event, userId);
        return mapper.toFullDto(event);
    }

    @Override
    public List<EventFullDto> getUserEvents(PrivateEventParams params, HttpServletRequest request) {
        logRequest(request);
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        Specification<Event> spec = buildSpecification(params);
        return eventRepository.findAll(spec, pageable).stream()
                .map(mapper::toFullDto)
                .toList();
    }

    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + id + " not found"));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with id " + id + " not found"));
    }

    private Specification<Event> buildSpecification(PrivateEventParams params) {
        return Specification.
                where(PrivateEventSpecifications.byInitiator(params.getUserId()))
                .and(PrivateEventSpecifications.byStates(params.getStates()))
                .and(PrivateEventSpecifications.hasText(params.getText()))
                .and(PrivateEventSpecifications.inCategories(params.getCategories()))
                .and(PrivateEventSpecifications.isPaid(params.getPaid()))
                .and(PrivateEventSpecifications.startAfter(params.getRangeStart()))
                .and(PrivateEventSpecifications.endBefore(params.getRangeEnd()));
    }

    private void checkOwnership(Event event, Long userId) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Can manage only own events");
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

    private void logRequest(HttpServletRequest request) {
        hitLoggingService.logRequest(request.getRequestURI(), request.getRemoteAddr());
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate == null) {

        }
    }
}
