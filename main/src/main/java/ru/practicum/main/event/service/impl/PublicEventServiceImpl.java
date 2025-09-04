package ru.practicum.main.event.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.dto.params.PublicEventParams;
import ru.practicum.main.event.dto.mapper.EventMapper;
import ru.practicum.main.event.entity.Event;
import ru.practicum.main.event.entity.EventState;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.event.repository.specification.PublicEventSpecifications;
import ru.practicum.main.event.service.PublicEventService;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.EntityNotFoundException;
import ru.practicum.main.request.entity.RequestStatus;
import ru.practicum.main.stats.view.EventViewService;
import ru.practicum.main.stats.hit.HitLoggingService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final EventMapper mapper;
    private final HitLoggingService hitLoggingService;
    private final EventViewService eventViewService;


    @Override
    public List<EventShortDto> getEvents(PublicEventParams params, HttpServletRequest request) {
        validateDateRange(params);
        List<Event> events = findEvents(params);
        Map<Long, Long> views = eventViewService.getViewsForEvents(events);

        List<EventShortDto> dto = mapToDto(events, views);
        return sortDto(dto, params.getSort());
    }

    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = getEventOrThrow(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            log.warn("Event with id={} not found", event.getId());
            throw new EntityNotFoundException("Event " + event + " not found");
        }

        logRequest(request);

        Map<Long, Long> views = eventViewService.getViewsForEvents(List.of(event));
        return enrichEventDto(event, views);
    }

    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("EventId={} not found", id);
                    return new EntityNotFoundException("Event with id " + id + " not found");
                });
    }

    private void logRequest(HttpServletRequest request) {
        hitLoggingService.logRequest(request.getRequestURI(), request.getRemoteAddr());
    }

    private List<Event> findEvents(PublicEventParams params) {
        Specification<Event> spec = buildSpecification(params);
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        return eventRepository.findAll(spec, pageable).getContent();
    }

    private Specification<Event> buildSpecification(PublicEventParams params) {
        return Specification
                .where(PublicEventSpecifications.isPublished())
                .and(PublicEventSpecifications.hasText(params.getText()))
                .and(PublicEventSpecifications.inCategories(params.getCategories()))
                .and(PublicEventSpecifications.isPaid(params.getPaid()))
                .and(PublicEventSpecifications.startAfter(params.getRangeStart()))
                .and(PublicEventSpecifications.endBefore(params.getRangeEnd()))
                .and(PublicEventSpecifications.onlyAvailable(params.getOnlyAvailable()));
    }

    private List<EventShortDto> mapToDto(List<Event> events, Map<Long, Long> views) {
        return events.stream()
                .map(event -> {
                    EventShortDto dto = mapper.toShortDto(event);
                    dto.setViews(views.getOrDefault(event.getId(), 0L));
                    return dto;
                })
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

    private void validateDateRange(PublicEventParams params) {
        if (params.getRangeStart() != null
                && params.getRangeEnd() != null
                && params.getRangeEnd().isBefore(params.getRangeStart())) {
            log.warn("Invalid params: rangeEnd={} before rangeStart={}", params.getRangeEnd(), params.getRangeStart());
            throw new BadRequestException("rangeEnd must be after rangeStart");
        }
    }

    private List<EventShortDto> sortDto(List<EventShortDto> dto, String sort) {
        if ("VIEWS".equalsIgnoreCase(sort)) {
            return dto.stream()
                    .sorted(Comparator.comparingLong(EventShortDto::getViews).reversed())
                    .toList();
        }
        return dto.stream()
                .sorted(Comparator.comparing(EventShortDto::getEventDate))
                .toList();
    }

}
