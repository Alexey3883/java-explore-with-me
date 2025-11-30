package ru.practicum.service;

import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    EndpointHitDto hit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStats(LocalDateTime startTime,
                                LocalDateTime endTime,
                                String uris,
                                Boolean unique);
}