package ru.practicum.stats.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.EndpointHitCreateDto;
import ru.practicum.stats.dto.EndpointHitResponseDto;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class StatsController {

    private final StatsService service;

    @PostMapping("/hit")
    public ResponseEntity<EndpointHitResponseDto> savedHit(@RequestBody @Valid EndpointHitCreateDto create) {
        EndpointHitResponseDto created = service.saveHit(create);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStats>> getStats(@RequestParam("start")
                                                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                                 LocalDateTime start,
                                                    @RequestParam("end")
                                                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                                 LocalDateTime end,
                                                    @RequestParam(value = "uris", required = false)
                                                                 List<String> uris,

                                                    @RequestParam(value = "unique", defaultValue = "false")
                                                                 boolean unique) {
        List<ViewStats> result = service.getStats(start, end, uris, unique);
        return ResponseEntity.ok(result);
    }

}
