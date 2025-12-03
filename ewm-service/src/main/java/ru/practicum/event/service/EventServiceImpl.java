package ru.practicum.event.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.EndpointHitDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.IllegalArgumentException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.UpdateEventIncorrectDataException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.utils.HitsEventViewUtil.getHitsEvent;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final CompilationRepository compilationRepository;
    private final CompilationService compilationService;
    private final UserServiceImpl userServiceImpl;
    private final StatsClient statsClient;
    private final CategoryMapper categoryMapper;
    private final RequestMapper requestMapper;

    @Override
    public EventFullDto addEventPrivate(Long userId, NewEventDto newEventDto) {

        if (newEventDto.getEventDate() != null
                && newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))
        ) {
            throw new ValidationException("Событие не удовлетворяет правилам создания");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory()).orElseThrow();

        Event event = eventMapper.toEvent(newEventDto, category, user);

        if (newEventDto.getParticipantLimit() != null && newEventDto.getParticipantLimit() < 0) {
            throw new ValidationException("Participant limit cannot be negative");
        }

        Event newEvent = eventRepository.save(event);

        log.info("Событие добавлено");
        return eventMapper.toFull(newEvent, 0L);
    }

    @Override
    @Transactional
    public List<EventShortDto> getEventsByUserIdPrivate(Long userId, Pageable pageable) {

        log.info("Получение событий пользователя");
        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(e -> eventMapper.toShort(e, getHitsEvent(
                        e.getId(),
                        LocalDateTime.now().minusDays(365),
                        LocalDateTime.now(), false, statsClient)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto getEventByIdPrivate(Long userId, Long eventId) {
        log.info("Получен запрос на получение полной информации о событии пользователя");
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId);

        Long view = getHitsEvent(
                event.getId(),
                LocalDateTime.now().minusDays(100),
                LocalDateTime.now(), false, statsClient
        );

        return eventMapper.toFull(event, view);
    }

    @Override
    @Transactional
    public EventFullDto updateEventPrivate(Long userId,
                                           Long eventId,
                                           UpdateEventUserRequest updateEventUserRequest
    ) {
        log.info("Изменение события пользователем");
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId);

        if (event.getState() == EventState.PUBLISHED) {
            throw new IllegalArgumentException("Пользователь не может изменять опубликованное событие");
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь не найден")
        );

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new IllegalArgumentException("Должно быть PENDING или CANCELED");
        }

        log.info("Получен запрос на изменение события пользователя");
        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }

        if (updateEventUserRequest.getCategory() != null) {
            event.setCategory(categoryRepository.getById(updateEventUserRequest.getCategory()));
        }

        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }

        if (updateEventUserRequest.getEventDate() != null) {
            if (updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("Начало события должно быть не менее 2 часов от его создания");
            } else {
                event.setEventDate(updateEventUserRequest.getEventDate());
            }
        }

        if (updateEventUserRequest.getLocation() != null) {
            if (updateEventUserRequest.getLocation().getLat() != null) {
                event.setLat(updateEventUserRequest.getLocation().getLat());
            }
            if (updateEventUserRequest.getLocation().getLon() != null) {
                event.setLon(updateEventUserRequest.getLocation().getLon());
            }
        }

        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }

        if (updateEventUserRequest.getParticipantLimit() != null) {
            if (updateEventUserRequest.getParticipantLimit() < 0) {
                throw new ValidationException("Participant limit cannot be negative");
            }
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }

        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }

        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction().equals("SEND_TO_REVIEW")) {
                event.setState(EventState.PENDING);
            } else if (updateEventUserRequest.getStateAction().equals("CANCEL_REVIEW")) {
                event.setState(EventState.CANCELED);
            } else {
                throw new IllegalArgumentException(
                        "Событие должно иметь статус PENDING при создании и статус CANCELED после выполнения запроса"
                );
            }
        }

        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }

        log.info("Событие изменено");
        return eventMapper.toFull(eventRepository.save(event), getHitsEvent(eventId,
                LocalDateTime.now().minusDays(100),
                LocalDateTime.now(),
                false,
                statsClient));

    }

    @Override
    @Transactional
    public List<EventFullDto> getEventsAdmin(EventAdminParam eventAdminParam) {
        Specification<Event> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (eventAdminParam.getUsers() != null) {
                CriteriaBuilder.In<User> usersInClause = criteriaBuilder.in(root.get("initiator"));
                for (Long userId : eventAdminParam.getUsers()) {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
                    usersInClause.value(user);
                }
                predicates.add(usersInClause);
            }

            if (eventAdminParam.getStates() != null) {
                EventState eventState;
                List<EventState> eventStates = new ArrayList<>();
                try {
                    for (String state : eventAdminParam.getStates()) {
                        eventState = EventState.valueOf(state);
                        eventStates.add(eventState);
                    }
                } catch (IllegalArgumentException e) {
                    throw new ValidationException("Unknown parameter of state");
                }
                CriteriaBuilder.In<EventState> statesInClause = criteriaBuilder.in(root.get("state"));
                for (EventState st : eventStates) {
                    statesInClause.value(st);
                }
                predicates.add(statesInClause);
            }

            if (eventAdminParam.getCategories() != null) {
                CriteriaBuilder.In<Category> categoriesInClause = criteriaBuilder.in(root.get("category"));
                for (Long categoryId : eventAdminParam.getCategories()) {
                    Category category = categoryRepository
                            .findById(categoryId).orElseThrow(() -> new NotFoundException("Категория не найдена"));
                    categoriesInClause.value(category);
                }
                predicates.add(categoriesInClause);
            }

            if (eventAdminParam.getRangeStart() != null) {
                predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), eventAdminParam.getRangeStart()));
            }

            if (eventAdminParam.getRangeEnd() != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("eventDate"), eventAdminParam.getRangeEnd()));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
        PageRequest pageable = PageRequest.of(eventAdminParam.getFrom() / eventAdminParam.getSize(),
                eventAdminParam.getSize(),
                Sort.by("id"));
        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        return events.stream().map(e -> eventMapper.toFull(e, getHitsEvent(e.getId(),
                        LocalDateTime.now().minusDays(1000),
                        LocalDateTime.now(), false, statsClient)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {

        log.info("Получен запрос на обновление события админом");
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие не найдено или недоступно")
        );

        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction().equals("PUBLISH_EVENT")) {
                if (!String.valueOf(event.getState()).equals("PENDING")) {
                    throw new IllegalArgumentException("Состояние события должно быть PENDING");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateEventAdminRequest.getStateAction().equals("REJECT_EVENT")) {
                if (String.valueOf(event.getState()).equals("PUBLISHED")) {
                    throw new IllegalArgumentException("Событие не может быть REJECT");
                }
                event.setState(EventState.CANCELED);
            } else {
                throw new IllegalArgumentException("StateAction должно быть PUBLISH_EVENT или REJECT_EVENT");
            }
        }

        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }

        if (updateEventAdminRequest.getCategory() != null
                && categoryRepository.existsById(updateEventAdminRequest.getCategory())) {
            Category category = categoryRepository
                    .findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            event.setCategory(category);
        }

        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }

        if (updateEventAdminRequest.getEventDate() != null) {
            if (updateEventAdminRequest.getEventDate().isBefore(LocalDateTime.now().minusHours(1))) {
                throw new UpdateEventIncorrectDataException(
                        "Дата начала изменяемого события должна быть не ранее чем за час от даты публикации"
                );
            }
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }

        if (updateEventAdminRequest.getLocation() != null) {
            event.setLon(updateEventAdminRequest.getLocation().getLon());
            event.setLat(updateEventAdminRequest.getLocation().getLat());
        }

        if (updateEventAdminRequest.getParticipantLimit() != null) {
            if (updateEventAdminRequest.getParticipantLimit() < 0) {
                throw new ValidationException("Participant limit cannot be negative");
            }
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }

        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }

        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }

        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }

        event.setId(eventId);
        eventRepository.save(event);

        return eventMapper.toFull(event, getHitsEvent(eventId,
                LocalDateTime.now().minusDays(100),
                LocalDateTime.now(),
                false,
                statsClient));
    }

    @Override
    @Transactional
    public List<EventShortDto> getEventsPublic(EventUserParam eventUserParam, HttpServletRequest request) {
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setIp(request.getRemoteAddr());
        endpointHitDto.setUri(request.getRequestURI());
        endpointHitDto.setApp("ewm-main-service");
        endpointHitDto.setTimestamp(LocalDateTime.now());
        statsClient.hit(endpointHitDto);

        Specification<Event> specification = (((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (eventUserParam.getCategories() != null) {
                CriteriaBuilder.In<Category> categoriesInClause = criteriaBuilder.in(root.get("category"));
                for (Long categoryId : eventUserParam.getCategories()) {
                    Category category = categoryRepository
                            .findById(categoryId).orElseThrow(() -> new ValidationException("Категория не найдена"));
                    categoriesInClause.value(category);
                }
                predicates.add(categoriesInClause);
            }
            if (eventUserParam.getPaid() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paid"), eventUserParam.getPaid()));
            }

            if (eventUserParam.getRangeStart() == null && eventUserParam.getRangeEnd() == null) {
                predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), LocalDateTime.now()));
            } else {

                if (eventUserParam.getRangeStart() != null &&
                        eventUserParam.getRangeEnd() != null &&
                        eventUserParam.getRangeStart().isAfter(eventUserParam.getRangeEnd())) {
                    throw new ValidationException("Дата начала не может быть позже даты окончания");
                } else if (eventUserParam.getRangeStart() != null) {
                    predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), eventUserParam.getRangeStart()));
                } else {
                    predicates.add(criteriaBuilder.lessThan(root.get("eventDate"), eventUserParam.getRangeEnd()));
                }
            }

            if (eventUserParam.getOnlyAvailable() != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("confirmedRequests"), root.get("participantLimit")));
            }
            predicates.add(criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED));

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }));

        if (eventUserParam.getSort() == null) {
            Pageable pageable = PageRequest.of(eventUserParam.getFrom() / eventUserParam.getSize(),
                    eventUserParam.getSize(),
                    Sort.by("id"));
            return getOutputEventsStream(specification, pageable);

        } else if (eventUserParam.getSort().equals(String.valueOf(EventSort.EVENT_DATE))) {
            Pageable pageable = PageRequest.of(eventUserParam.getFrom() / eventUserParam.getSize(),
                    eventUserParam.getSize(),
                    Sort.by("eventDate"));
            return getOutputEventsStream(specification, pageable);

        } else if (eventUserParam.getSort().equals(String.valueOf(EventSort.VIEWS))) {
            Pageable pageable = PageRequest.of(eventUserParam.getFrom() / eventUserParam.getSize(),
                    eventUserParam.getSize(),
                    Sort.unsorted());
            List<EventShortDto> events = getOutputEventsStream(specification, pageable);

            return events.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews, Comparator.reverseOrder()))
                    .collect(Collectors.toList());
        }
        throw new ValidationException("Сортировака может быть EVENT_DATE или VIEWS");
    }

    public List<EventShortDto> getOutputEventsStream(Specification<Event> specification, Pageable pageable) {
        List<Event> allEvents = eventRepository.findAll(specification, pageable).getContent();
        return allEvents.stream()
                .map(r -> eventMapper.toShort(r, getHitsEvent(r.getId(),
                        LocalDateTime.now().minusDays(1000),
                        LocalDateTime.now(), true, statsClient)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto getEventByIdPublic(Long eventId, HttpServletRequest request) {

        log.info("Получен запрос на получение события по id");

        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setIp(request.getRemoteAddr());
        endpointHitDto.setUri(request.getRequestURI());
        endpointHitDto.setApp("ewm-main-service");
        endpointHitDto.setTimestamp(LocalDateTime.now());
        statsClient.hit(endpointHitDto);

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED);

        if (event == null) {
            throw new NotFoundException(String.format("Event with id=%d was not found", eventId));
        }

        Long view = getHitsEvent(
                event.getId(),
                LocalDateTime.now().minusDays(1000),
                LocalDateTime.now(), true, statsClient
        );

        log.info("Событие {} получено", event.getTitle());
        return eventMapper.toFull(event, view);
    }

    @Override
    @Transactional
    public List<ParticipationRequestDto> getRequestsUserToEventPrivate(Long userId, Long eventId) {

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format("User with id=%d not found", userId)));

        log.info("Запросы получены");
        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestStatusPrivate(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest updateRequest
    ) {
        EventRequestStatusUpdateResult updateResult;
        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();
        int countRequests = updateRequest.getRequestIds().size();
        List<Request> requests = requestRepository.findByIdIn(updateRequest.getRequestIds());

        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format("User with id=%d not found", userId)));

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие не найдено");
        }
        for (Request request : requests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new IllegalArgumentException("Статус запроса не PENDING");
            }
        }
        if (updateRequest.getStatus() != null) {
            switch (updateRequest.getStatus()) {
                case "CONFIRMED":
                    if (event.getParticipantLimit() == 0 || !event.getRequestModeration()
                            || event.getParticipantLimit() > event.getConfirmedRequests() + countRequests) {
                        requests.forEach(request -> request.setStatus(RequestStatus.CONFIRMED));
                        event.setConfirmedRequests(event.getConfirmedRequests() + countRequests);
                        confirmedRequests.addAll(requests);

                    } else if (event.getParticipantLimit() <= event.getConfirmedRequests()) {
                        throw new IllegalArgumentException("Participant Limit");
                    } else {
                        for (Request request : requests) {
                            if (event.getParticipantLimit() > event.getConfirmedRequests()) {
                                request.setStatus(RequestStatus.CONFIRMED);
                                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                                confirmedRequests.add(request);
                            } else {
                                request.setStatus(RequestStatus.REJECTED);
                                rejectedRequests.add(request);
                            }
                        }
                    }
                    break;
                case "REJECTED":
                    requests.forEach(request -> request.setStatus(RequestStatus.REJECTED));
                    rejectedRequests.addAll(requests);
            }
        }
        eventRepository.save(event);
        requestRepository.saveAll(requests);

        List<ParticipationRequestDto> confirmed = confirmedRequests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
        List<ParticipationRequestDto> rejected = rejectedRequests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
        updateResult = new EventRequestStatusUpdateResult(confirmed, rejected);
        log.info("Запрос обновлен");
        return updateResult;
    }

}