package ru.practicum.main.stats.hit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.stats.dto.EndpointHitCreateDto;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class HitLoggingServiceImpl implements HitLoggingService {

    private final StatsClient statsClient;

    @Value("${app.name}")
    private String appName;

    @Override
    public void logRequest(String uri, String ip) {
        EndpointHitCreateDto hit = new EndpointHitCreateDto(
                appName,
                uri,
                ip,
                LocalDateTime.now()
        );
        statsClient.sendHit(hit);
    }
}
