package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndPointHitCreateDto;
import ru.practicum.stats.dto.EndPointHitResponseDto;
import ru.practicum.stats.dto.mapper.StatsMapper;
import ru.practicum.stats.entity.EndpointHit;
import ru.practicum.stats.repository.StatsRepository;


import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository repository;
    private final StatsMapper mapper;


    @Override
    public EndPointHitResponseDto saveHit(EndPointHitCreateDto create) {
        EndpointHit entity = mapper.toEntity(create);
        EndpointHit saved = repository.save(entity);
        log.info("Hit={} saved", saved);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EndPointHitResponseDto> getStats(LocalDateTime start,
                                                 LocalDateTime end,
                                                 List<String> uris,
                                                 boolean unique) {
        return repository.findStatsByCriteria(start, end, uris, unique);
    }
}
