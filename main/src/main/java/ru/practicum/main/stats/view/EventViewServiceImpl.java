package ru.practicum.main.stats.view;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.main.event.entity.Event;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventViewServiceImpl implements EventViewService {

    private final StatsClient statsClient;

    public Map<Long, Long> getViewsForEvents(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        LocalDateTime start = events.stream()
                .map(Event::getPublishedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        List<ViewStats> stats = statsClient.getStats(
                start,
                LocalDateTime.now(),
                uris,
                true
        );

        return stats.stream()
                .collect(Collectors.toMap(
                        s -> Long.parseLong(s.getUri().replace("/events/", "")),
                        ViewStats::getHits
                ));
    }
}
