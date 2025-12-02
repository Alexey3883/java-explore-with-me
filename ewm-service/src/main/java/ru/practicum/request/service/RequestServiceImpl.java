package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.IllegalArgumentException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;

    @Override
    @Transactional
    public ParticipationRequestDto addRequestPrivate(Long userId, Long eventId) {
        log.info("Добавление запроса");
        Request request = new Request();
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие не найдено"));
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь не найден"));
        List<Request> requests = requestRepository.findAllByRequesterIdAndEventId(userId, eventId);
        if (!requests.isEmpty()) {
            throw new IllegalArgumentException("Запрос c такими данными уже существует");
        }
        if (userId.equals(event.getInitiator().getId())) {
            throw new IllegalArgumentException("Инициатор запроса не найден");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new IllegalArgumentException("Событие не опубликовано");
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            throw new IllegalArgumentException("Participant Limit");
        }

        request.setRequester(user);
        request.setEvent(event);
        request.setStatus(RequestStatus.PENDING);
        request.setCreated(LocalDateTime.now());

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        log.info("Добавлен запрос пользователя");
        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsPrivate(Long userId) {
        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь не найден"));

        log.info("Get user requests to event");
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь не найден"));

        Request request = requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Запрос не найден"));
        request.setStatus(RequestStatus.CANCELED);
        request = requestRepository.save(request);
        log.info("Canceled request");
        return requestMapper.toDto(request);
    }

}