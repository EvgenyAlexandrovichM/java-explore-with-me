package ru.practicum.stats.service;

import ru.practicum.stats.dto.EndpointHitCreateDto;
import ru.practicum.stats.dto.EndpointHitResponseDto;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    EndpointHitResponseDto saveHit(EndpointHitCreateDto create);

    List<ViewStats> getStats(LocalDateTime from, LocalDateTime to, List<String> uris, boolean unique);
}
