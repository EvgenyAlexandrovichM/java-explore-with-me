package ru.practicum.stats.repository;

import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;


public interface StatsRepositoryCriteria {
    List<ViewStats> findStatsByCriteria(LocalDateTime start,
                                        LocalDateTime end,
                                        List<String> uris,
                                        boolean unique);

}
