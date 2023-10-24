package ru.practicum.ewm.controller.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.service.EventService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/events")
public class EventControllerPrivate {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId, @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Получен POST запрос от пользователя с id{} к /users/{userId}/events: {}", userId, newEventDto);
        return eventService.create(userId, newEventDto);
    }

    @GetMapping
    public List<EventShortDto> getEvents(@PathVariable Long userId,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) {
        log.info("Получен Get запрос к /users/{userId}/events от пользователя с id{}", userId);
        return eventService.getEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable Long userId,
                                     @PathVariable Long eventId) {
        log.info("Получен Get запрос от пользователя с id{} к /users/{userId}/events/{eventId}: eventId{}", userId, eventId);
        return eventService.getEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("Получен Patch запрос от пользователя с id{} к /users/{userId}/events/{eventId}: eventId{}", userId, eventId);
        return eventService.updateEvent(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByEvent(@PathVariable Long userId,
                                                            @PathVariable Long eventId) {
        log.info("Получен Get запрос от пользователя с id{} к /users/{userId}/events/{eventId}/requests: eventId{}", userId, eventId);
        return eventService.getRequestsByEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    EventRequestStatusUpdateResult updateEventRequest(@PathVariable Long userId,
                                                      @PathVariable Long eventId,
                                                      @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("Получен Patch запрос от пользователя с id{} к /users/{userId}/events/{eventId}/requests: eventId{}", userId, eventId);
        return eventService.updateEventRequest(userId, eventId, eventRequestStatusUpdateRequest);
    }
}
