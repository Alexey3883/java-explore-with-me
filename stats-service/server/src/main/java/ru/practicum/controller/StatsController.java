package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.service.StatsService;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.practicum.exception.ValidationException;
import ru.practicum.exception.ErrorResponse;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto hit(@RequestBody EndpointHitDto endpointHitDto) {
        log.info("Получен запрос на сохранение эндпоинта");
        return statsService.hit(endpointHitDto);
    }

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<ViewStatsDto> getStats(
            @RequestParam("start")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime start,

            @RequestParam("end")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime end,

            @RequestParam(name = "uris", required = false) String uris,
            @RequestParam(name = "unique", required = false, defaultValue = "false") Boolean unique) {

        return statsService.getStats(start, end, uris, unique);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final ValidationException e) {
        return new ErrorResponse(e.getMessage());
    }

}