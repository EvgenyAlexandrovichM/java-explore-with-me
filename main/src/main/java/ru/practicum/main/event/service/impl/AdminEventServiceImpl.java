package ru.practicum.main.event.service.impl;

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
import ru.practicum.main.event.dto.StateAction;
import ru.practicum.main.event.dto.mapper.EventMapper;
import ru.practicum.main.event.dto.params.AdminEventParams;
import ru.practicum.main.event.entity.Event;
import ru.practicum.main.event.entity.EventState;
import ru.practicum.main.event.entity.location.Location;
import ru.practicum.main.event.dto.location.LocationDto;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.event.repository.specification.AdminEventSpecifications;
import ru.practicum.main.event.service.AdminEventService;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.EntityNotFoundException;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper mapper;

    @Override
    public List<EventFullDto> searchEvents(AdminEventParams params) {
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());

        Specification<Event> spec = buildSpecification(params);

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        return events.stream()
                .map(mapper::toFullDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, EventUpdateDto updateDto) {
        Event event = getEventOrThrow(eventId);

        updateBasicFields(event, updateDto);
        log.info("Basic fields updated for event id={}", event.getId());
        updateCategoryIfNeeded(event, updateDto.getCategoryId());
        log.info("Category={} updated for event id={}", event.getCategory(), event.getId());
        updateLocationIfNeeded(event, updateDto.getLocation());
        log.info("Location={} updated for event id={}", event.getLocation(), event.getId());
        updateStateIfNeeded(event, updateDto.getStateAction());
        log.info("State={} updated for event id={}", event.getState(), event.getId());

        log.info("Event with id={} updated", event.getId());
        return mapper.toFullDto(eventRepository.save(event));
    }

    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("EventId={} not found", id);
                    return new EntityNotFoundException("Event with id " + id + " not found");
                });
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("CategoryId={} not found", id);
                    return new EntityNotFoundException("Category with id " + id + " not found");
                });
    }

    private Specification<Event> buildSpecification(AdminEventParams params) {
        return Specification
                .where(AdminEventSpecifications.byUsers(params.getUserIds()))
                .and(AdminEventSpecifications.byStates(params.getStates()))
                .and(AdminEventSpecifications.inCategories(params.getCategoryIds()))
                .and(AdminEventSpecifications.startAfter(params.getRangeStart()))
                .and(AdminEventSpecifications.endBefore(params.getRangeEnd()));
    }

    private void updateBasicFields(Event event, EventUpdateDto dto) {
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) event.setEventDate(dto.getEventDate());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
    }

    private void updateCategoryIfNeeded(Event event, Long categoryId) {
        if (categoryId != null) {
            Category category = getCategoryOrThrow(categoryId);
            event.setCategory(category);
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

    private void updateStateIfNeeded(Event event, StateAction stateAction) {
        if (stateAction == null) {
            return;
        }
        if (stateAction == StateAction.PUBLISH_EVENT) {
            if (!event.getState().equals(EventState.PENDING)) {
                log.warn("Event state={} cannot be published", event.getState());
                throw new ConflictException("Can publish only events in status PENDING");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }

        if (stateAction == StateAction.REJECT_EVENT) {
            if (event.getState().equals(EventState.PUBLISHED)) {
                log.warn("Event state={} cannot be rejected", event.getState());
                throw new ConflictException("Cannot reject already published event");
            }
            event.setState(EventState.REJECTED);
        }
    }
}
