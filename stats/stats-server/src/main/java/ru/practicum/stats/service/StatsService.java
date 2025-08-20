package ru.practicum.stats.service;

import ru.practicum.stats.dto.EndPointHitCreateDto;
import ru.practicum.stats.dto.EndPointHitResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    EndPointHitResponseDto saveHit(EndPointHitCreateDto create);

    List<EndPointHitResponseDto> getStats(LocalDateTime from, LocalDateTime to, List<String> uris, boolean unique);
}
