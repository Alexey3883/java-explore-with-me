package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.event.dto.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface EventService {

    EventFullDto addEventPrivate(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEventsByUserIdPrivate(Long userId, Pageable pageable);

    EventFullDto getEventByIdPrivate(Long userId, Long eventId);

    EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<EventFullDto> getEventsAdmin(EventAdminParam eventAdminParam);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> getEventsPublic(EventUserParam eventUserParam, HttpServletRequest request);

    EventFullDto getEventByIdPublic(Long eventId, HttpServletRequest request);

    List<ParticipationRequestDto> getRequestsUserToEventPrivate(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequestStatusPrivate(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest updateRequests);

}