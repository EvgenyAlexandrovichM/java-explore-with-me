package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitCreateDto;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.utils.DateTimeUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatsClient {

    private final RestTemplate restTemplate;

    @Value("${stats-server.url}")
    private String serverUrl;

    public void sendHit(EndpointHitCreateDto hit) {
        restTemplate.postForEntity(serverUrl + "/hit", hit, Void.class);
    }

    public List<ViewStats> getStats(LocalDateTime start,
                                    LocalDateTime end,
                                    List<String> uris,
                                    boolean unique) {


        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", start.format(DateTimeUtils.FORMATTER))
                .queryParam("end", end.format(DateTimeUtils.FORMATTER))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            uris.forEach(u -> builder.queryParam("uris", u));
        }

        ResponseEntity<ViewStats[]> response = restTemplate.getForEntity(
                builder.toUriString(),
                ViewStats[].class
        );

        return Optional.ofNullable(response.getBody())
                .map(Arrays::asList)
                .orElse(List.of());


    }
}
