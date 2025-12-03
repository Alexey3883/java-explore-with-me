package ru.practicum.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class HitsEventViewUtil {

    public static Long getHitsEvent(Long eventId, LocalDateTime start, LocalDateTime end, Boolean unique, StatsClient statsClient) {

        if (start.isAfter(end)) {
            throw new ValidationException("Start date cannot be after end date");
        }

        List<String> uris = new ArrayList<>();
        uris.add("/events/" + eventId);

        String urisString = String.join(",", uris);

        ResponseEntity<Object> response = statsClient.getStats(start, end, urisString, unique);

        Object responseBody = response.getBody();

        List<ViewStatsDto> output = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

        if (responseBody != null) {
            try {
                output = mapper.convertValue(responseBody, new TypeReference<List<ViewStatsDto>>() {});
            } catch (IllegalArgumentException e) {
                output = new ArrayList<>();
            }
        }

        Long view = 0L;

        if (!output.isEmpty() && output.get(0) != null && output.get(0).getHits() != null) {
            view = output.get(0).getHits();
        }
        return view;

    }
}