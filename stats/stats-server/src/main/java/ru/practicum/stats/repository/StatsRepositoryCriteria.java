package ru.practicum.stats.repository;

import ru.practicum.stats.dto.EndPointHitResponseDto;

import java.time.LocalDateTime;
import java.util.List;


public interface StatsRepositoryCriteria {
    List<EndPointHitResponseDto> findStatsByCriteria(LocalDateTime start,
                                                     LocalDateTime end,
                                                     List<String> uris,
                                                     boolean unique);

}
