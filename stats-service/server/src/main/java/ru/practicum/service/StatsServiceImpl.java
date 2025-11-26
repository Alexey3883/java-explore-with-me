package ru.practicum.service;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
@RequiredArgsConstructor
@Service
public class StatsServiceImpl implements StatsService {

    private final EndpointHitRepository endpointHitRepository;
    private final EndpointHitMapper endpointHitMapper;

    @Override
    public EndpointHitDto hit(EndpointHitDto endpointHitDto) {
        if (endpointHitDto == null) return null;
        endpointHitRepository.save(endpointHitMapper.toEndpointHit(endpointHitDto));
        log.info("Информация сохранена");
        return endpointHitDto;
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime startTime,
                                       LocalDateTime endTime,
                                       String uris,
                                       Boolean unique) {
        log.info("Получена статистика по параметрам: Start({}), End({}), Uris({}), Unique({})",
                startTime, endTime, uris, unique);

        // Проверка на корректность дат
        if (startTime.isAfter(endTime)) {
            throw new ValidationException("Время начала не может быть позже времени окончания");
        }

        List<String> uriList = new ArrayList<>();

        // Разбор параметра uris
        if (uris != null && !uris.isEmpty()) {
            // Разбиваем строку по запятым и убираем пробелы
            String[] urisArray = uris.split(",");
            for (String uri : urisArray) {
                String trimmedUri = uri.trim();
                if (!trimmedUri.isEmpty()) {
                    uriList.add(trimmedUri);
                }
            }
        }

        List<ViewStatsDto> listViewStatsDto = new ArrayList<>();

        if (!uriList.isEmpty()) {
            List<EndpointHit> hits;
            if (unique != null && unique) {
                // Получаем все хиты в заданном диапазоне и URI
                hits = endpointHitRepository.findAllStatsByTimestampBetweenAndUriIn(startTime, endTime, uriList);

                // Группируем по URI и считаем уникальные IP
                Map<String, List<EndpointHit>> groupedByUri = hits.stream()
                        .collect(Collectors.groupingBy(EndpointHit::getUri));

                // Создаем результаты
                for (Map.Entry<String, List<EndpointHit>> entry : groupedByUri.entrySet()) {
                    String uri = entry.getKey();
                    List<EndpointHit> uriHits = entry.getValue();

                    // Считаем уникальные IP
                    long uniqueCount = uriHits.stream()
                            .map(EndpointHit::getIp)
                            .distinct()
                            .count();

                    ViewStatsDto dto = new ViewStatsDto();
                    dto.setApp(uriHits.get(0).getApp());
                    dto.setUri(uri);
                    dto.setHits(uniqueCount);
                    listViewStatsDto.add(dto);
                }
            } else {
                // Получаем все хиты в заданном диапазоне и URI
                hits = endpointHitRepository.findAllStatsByTimestampBetweenAndUriIn(startTime, endTime, uriList);

                // Группируем по URI и считаем общее кол-во
                Map<String, List<EndpointHit>> groupedByUri = hits.stream()
                        .collect(Collectors.groupingBy(EndpointHit::getUri));

                // Создаем результаты
                for (Map.Entry<String, List<EndpointHit>> entry : groupedByUri.entrySet()) {
                    String uri = entry.getKey();
                    List<EndpointHit> uriHits = entry.getValue();

                    ViewStatsDto dto = new ViewStatsDto();
                    dto.setApp(uriHits.get(0).getApp());
                    dto.setUri(uri);
                    dto.setHits((long) uriHits.size());
                    listViewStatsDto.add(dto);
                }
            }
        } else {
            // Случай без фильтра по URI
            List<EndpointHit> hits;
            if (unique != null && unique) {
                hits = endpointHitRepository.findAllStatsByTimestampBetween(startTime, endTime);

                // Группируем по URI и считаем уникальные IP
                Map<String, List<EndpointHit>> groupedByUri = hits.stream()
                        .collect(Collectors.groupingBy(EndpointHit::getUri));

                // Создаем результаты
                for (Map.Entry<String, List<EndpointHit>> entry : groupedByUri.entrySet()) {
                    String uri = entry.getKey();
                    List<EndpointHit> uriHits = entry.getValue();

                    // Считаем уникальные IP
                    long uniqueCount = uriHits.stream()
                            .map(EndpointHit::getIp)
                            .distinct()
                            .count();

                    ViewStatsDto dto = new ViewStatsDto();
                    dto.setApp(uriHits.get(0).getApp());
                    dto.setUri(uri);
                    dto.setHits(uniqueCount);
                    listViewStatsDto.add(dto);
                }
            } else {
                hits = endpointHitRepository.findAllStatsByTimestampBetween(startTime, endTime);

                // Группирунм по URI и считаем общее количество
                Map<String, List<EndpointHit>> groupedByUri = hits.stream()
                        .collect(Collectors.groupingBy(EndpointHit::getUri));

                // Создаем результаты
                for (Map.Entry<String, List<EndpointHit>> entry : groupedByUri.entrySet()) {
                    String uri = entry.getKey();
                    List<EndpointHit> uriHits = entry.getValue();

                    ViewStatsDto dto = new ViewStatsDto();
                    dto.setApp(uriHits.get(0).getApp());
                    dto.setUri(uri);
                    dto.setHits((long) uriHits.size());
                    listViewStatsDto.add(dto);
                }
            }
        }

        // Сортируем по убыванию количество просмотров
        listViewStatsDto.sort(Comparator.comparing(ViewStatsDto::getHits).reversed());

        return listViewStatsDto;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

}