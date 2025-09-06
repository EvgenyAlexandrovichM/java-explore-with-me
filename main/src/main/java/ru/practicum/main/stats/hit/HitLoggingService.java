package ru.practicum.main.stats.hit;

public interface HitLoggingService {
    void logRequest(String uri, String ip);
}
