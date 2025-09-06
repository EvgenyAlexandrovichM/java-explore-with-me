package ru.practicum.main.stats.view;

import ru.practicum.main.event.entity.Event;

import java.util.List;
import java.util.Map;

public interface EventViewService {

    public Map<Long, Long> getViewsForEvents(List<Event> events);
}
