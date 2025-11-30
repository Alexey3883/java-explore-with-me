package ru.practicum.repository;

import ru.practicum.model.EndpointHit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {

    @Query("SELECT e FROM EndpointHit e WHERE e.timestamp BETWEEN :start AND :end AND e.uri IN :uris ORDER BY e.timestamp")
    List<EndpointHit> findAllStatsByTimestampBetweenAndUriIn(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);

    @Query("SELECT e FROM EndpointHit e WHERE e.timestamp BETWEEN :start AND :end ORDER BY e.timestamp")
    List<EndpointHit> findAllStatsByTimestampBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}