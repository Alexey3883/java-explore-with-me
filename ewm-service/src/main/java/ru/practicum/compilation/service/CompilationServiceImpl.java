package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.compilation.dto.*;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.utils.HitsEventViewUtil.getHitsEvent;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final StatsClient client;

    @Override
    @Transactional
    public CompilationDto addCompilationAdmin(NewCompilationDto newCompilationDto) {
        if (newCompilationDto == null) {
            throw new java.lang.IllegalArgumentException("Compilation data cannot be null");
        }

        if (newCompilationDto.getTitle() == null) {
            throw new java.lang.IllegalArgumentException("Compilation title cannot be null");
        }

        if (newCompilationDto.getTitle().trim().isEmpty()) {
            throw new java.lang.IllegalArgumentException("Compilation title cannot be empty or consist of spaces only");
        }

        if (newCompilationDto.getTitle().length() > 50) {
            throw new java.lang.IllegalArgumentException("Compilation title must be no more than 50 characters");
        }

        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());

        if (newCompilationDto.getEvents() != null) {
            List<Event> events = eventRepository.findAllByIdIn(newCompilationDto.getEvents());
            compilation.setEvents(events);
        } else {
            compilation.setEvents(new ArrayList<>());
        }

        if (newCompilationDto.getPinned() == null) {
            compilation.setPinned(false);
        } else {
            compilation.setPinned(newCompilationDto.getPinned());
        }

        compilationRepository.save(compilation);
        return compilationMapper.toDto(compilation, 0L);
    }

    @Override
    @Transactional
    public void deleteCompilationAdmin(Long compilationId) {
        if (!compilationRepository.existsById(compilationId)) {
            throw new NotFoundException(String.format("Compilation with id=%d was not found", compilationId));
        }
        compilationRepository.deleteById(compilationId);
        log.info(String.format("Compilation with id=%d deleted", compilationId));
    }

    @Override
    @Transactional
    public CompilationDto updateCompilationAdmin(Long compilationId, UpdateCompilationRequest updateCompilationRequest) {
        if (updateCompilationRequest == null) {
            throw new java.lang.IllegalArgumentException("Compilation data cannot be null");
        }

        if (updateCompilationRequest.getTitle() != null) {
            if (updateCompilationRequest.getTitle().trim().isEmpty()) {
                throw new java.lang.IllegalArgumentException("Compilation title cannot be empty or consist of spaces only");
            }

            if (updateCompilationRequest.getTitle().length() > 50) {
                throw new java.lang.IllegalArgumentException("Compilation title must be no more than 50 characters");
            }
        }

        Compilation compilation = getCompilationOrThrow(compilationId);

        if (updateCompilationRequest.getEvents() != null) {
            if (updateCompilationRequest.getEvents().isEmpty()) {
                compilation.setEvents(new ArrayList<>());
            } else {
                List<Event> events = eventRepository.findAllByIdIn(updateCompilationRequest.getEvents());
                compilation.setEvents(events);
            }
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        compilation = compilationRepository.save(compilation);
        Long view = getHitsEvent(compilationId, LocalDateTime.now().minusDays(1000),
                LocalDateTime.now(), true, client);

        if (view == null) {
            view = 0L;
        }
        return compilationMapper.toDto(compilation, view);
    }

    @Override
    public List<CompilationDto> getCompilationsPublic(Boolean pinned, Pageable pageable) {
        if (pinned == null) {
            return compilationRepository.findAll(pageable).stream()
                    .map(c -> compilationMapper.toDto(c, getHitsEvent(c.getId(),
                            LocalDateTime.now().minusDays(100),
                            LocalDateTime.now(), true, client)))
                    .collect(Collectors.toList());
        }

        return compilationRepository.findAllByPinned(pinned, pageable).stream()
                .map(c -> compilationMapper.toDto(c, getHitsEvent(c.getId(),
                        LocalDateTime.now().minusDays(100),
                        LocalDateTime.now(), true, client)))
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationPublic(Long compilationId) {
        Compilation compilation = getCompilationOrThrow(compilationId);
        return compilationMapper.toDto(compilation, getHitsEvent(compilationId,
                LocalDateTime.now().minusDays(100),
                LocalDateTime.now(), true, client));
    }

    private Compilation getCompilationOrThrow(Long compilationId) {
        return compilationRepository.findById(compilationId).orElseThrow(
                () -> new NotFoundException(String.format("Compilation with id=%d was not found", compilationId)));
    }

}