package ru.practicum.ewm.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.enums.*;
import ru.practicum.ewm.exception.*;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.*;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        User user = getUser(userId);
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException(newEventDto.getCategory()));
        isEventEarlier(newEventDto.getEventDate());
        Event event = EventMapper.toEvent(newEventDto);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setInitiator(user);
        event.setState(EventState.PENDING);
        event.setLocation(locationRepository.save(newEventDto.getLocation()));
        Event createdEvent = eventRepository.save(event);
        log.info("Добавлено событие с id{}", createdEvent.getId());
        return EventMapper.toEventFullDto(createdEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(Long userId, int from, int size) {
        getUser(userId);
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageRequest);
        log.info("Получено событий: {}", events.size());
        return events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(Long userId, Long eventId) {
        Event event = getEventByInitiatorAndEventId(userId, eventId);
        log.info("Получено событие с id{}", event.getId());
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event oldEvent = getEventByInitiatorAndEventId(userId, eventId);

        if (oldEvent.getState().equals(EventState.PUBLISHED)) {
            throw new EventAlreadyPublishedException("Изменить можно только отмененные события или события в состоянии ожидания модерации.");
        }
        if (updateEventUserRequest.getAnnotation() != null) {
            oldEvent.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException(updateEventUserRequest.getCategory()));
            oldEvent.setCategory(category);
        }
        if (updateEventUserRequest.getDescription() != null) {
            oldEvent.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getEventDate() != null) {
            isEventEarlier(updateEventUserRequest.getEventDate());
            oldEvent.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getPaid() != null) {
            oldEvent.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getLocation() != null) {
            oldEvent.setLocation(locationRepository.save(updateEventUserRequest.getLocation()));
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            oldEvent.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            oldEvent.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction().equals(StateAction.SEND_TO_REVIEW)) {
                oldEvent.setState(EventState.PENDING);
            } else {
                oldEvent.setState(EventState.CANCELED);
            }
        }
        if (updateEventUserRequest.getTitle() != null) {
            oldEvent.setTitle(updateEventUserRequest.getTitle());
        }
        Event updatedEvent = eventRepository.save(oldEvent);
        log.debug("Событие с id{} обновлено", updatedEvent.getId());
        return EventMapper.toEventFullDto(updatedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId) {
        getEventByInitiatorAndEventId(userId, eventId);
        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);
        log.info("Получено запросов на участие: {}", requests.size());
        return requests.stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest statusUpdateRequest) {
        Event oldEvent = getEventByInitiatorAndEventId(userId, eventId);

        if (!oldEvent.getRequestModeration() || oldEvent.getParticipantLimit() == 0) {
            throw new ParticipantLimitException("Лимит заявок равен 0 или отключена пре-модерация заявок, подтверждение заявок не требуется.");
        }
        Long countRequest = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (oldEvent.getParticipantLimit() != 0 && oldEvent.getParticipantLimit() <= countRequest) {
            throw new ParticipantLimitException("Достигнут лимит по заявкам на данное событие.");
        }
        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);
        List<ParticipationRequest> updateRequests = requests.stream()
                .filter(request -> statusUpdateRequest.getRequestIds().contains(request.getId()))
                .collect(Collectors.toList());
        if (updateRequests.isEmpty()) {
            throw new ParticipationRequestNotFoundException("Запросы на участие с id: " + statusUpdateRequest.getRequestIds() + " не найдены.");
        }
        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(new ArrayList<>())
                .build();
        for (ParticipationRequest request : updateRequests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new RequestStatusConflictException("Статус можно изменить только у заявок, находящихся в состоянии ожидания.");
            }
            switch (statusUpdateRequest.getStatus()) {
                case CONFIRMED:
                    if (countRequest < oldEvent.getParticipantLimit()) {
                        request.setStatus(RequestStatus.CONFIRMED);
                        result.getConfirmedRequests().add(ParticipationRequestMapper.toParticipationRequestDto(request));
                        countRequest++;
                    } else {
                        request.setStatus(RequestStatus.REJECTED);
                        result.getRejectedRequests().add(ParticipationRequestMapper.toParticipationRequestDto(request));
                        throw new ParticipantLimitException("Достигнут лимит по заявкам на данное событие.");
                    }
                    break;
                case REJECTED:
                    request.setStatus(RequestStatus.REJECTED);
                    result.getRejectedRequests().add(ParticipationRequestMapper.toParticipationRequestDto(request));
                    break;
            }
        }
        List<ParticipationRequest> updatedRequests = requestRepository.saveAll(updateRequests);
        log.info("Обновлено запросов на участие в событии: {}", updatedRequests.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> searchAdmin(List<Long> users, List<EventState> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByFiltersAdmin(users, states, categories, rangeStart, rangeEnd, pageRequest);
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }
        ResponseEntity<Object> views = statsClient.getStats(rangeStart, rangeEnd, uris, true);
        ObjectMapper objectMapper = new ObjectMapper();
        List<ViewStatsDto> viewStatsList = objectMapper.convertValue(views.getBody(), new TypeReference<>() {
        });
        List<EventFullDto> result = events.stream()
                .map(EventMapper::toEventFullDto)
                .peek(eventFullDto -> {
                    Optional<ViewStatsDto> viewStatsDto = viewStatsList.stream()
                            .filter(viewStatsDto1 -> viewStatsDto1.getUri().equals("/events/" + eventFullDto.getId()))
                            .findFirst();
                    eventFullDto.setViews(viewStatsDto.map(ViewStatsDto::getHits).orElse(0L));
                }).peek(eventFullDto -> eventFullDto.setConfirmedRequests(
                        requestRepository.countByEventIdAndStatus(eventFullDto.getId(), RequestStatus.CONFIRMED)))
                .collect(Collectors.toList());

        log.info("Найдено событий: {}", result.size());
        return result;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event oldEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(String.format("Событие с id%d не найдено.", eventId)));

        if (updateEventAdminRequest.getAnnotation() != null) {
            oldEvent.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException(updateEventAdminRequest.getCategory()));
            oldEvent.setCategory(category);
        }
        if (updateEventAdminRequest.getDescription() != null) {
            oldEvent.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getEventDate() != null) {
            LocalDateTime eventDateTime = updateEventAdminRequest.getEventDate();
            if (eventDateTime.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new WrongTimeException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации.");
            }
            oldEvent.setEventDate(updateEventAdminRequest.getEventDate());
        }
        if (updateEventAdminRequest.getPaid() != null) {
            oldEvent.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getLocation() != null) {
            oldEvent.setLocation(locationRepository.save(updateEventAdminRequest.getLocation()));
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            oldEvent.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            oldEvent.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }
        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction().equals(AdminStateAction.PUBLISH_EVENT)) {
                if (!oldEvent.getState().equals(EventState.PENDING)) {
                    throw new EventStateConflictException("Событие можно публиковать, только если оно в состоянии ожидания публикации.");
                }
                oldEvent.setState(EventState.PUBLISHED);
                oldEvent.setPublishedOn(LocalDateTime.now());
            } else if (updateEventAdminRequest.getStateAction().equals(AdminStateAction.REJECT_EVENT)) {
                if (oldEvent.getPublishedOn() != null) {
                    throw new EventAlreadyPublishedException("Событие можно отклонить, только если оно еще не опубликовано.");
                }
                oldEvent.setState(EventState.CANCELED);
            }
        }
        if (updateEventAdminRequest.getTitle() != null) {
            oldEvent.setTitle(updateEventAdminRequest.getTitle());
        }
        Event updatedEvent = eventRepository.save(oldEvent);
        log.debug("Событие с id{} обновлено", updatedEvent.getId());
        return EventMapper.toEventFullDto(updatedEvent);
    }

    @Override
    @Transactional
    public List<EventShortDto> searchPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd, Boolean onlyAvailable, EventSort sort, int from, int size) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd)) {
                throw new IncorrectParameterException("Некорректные параметры даты.");
            }
        }
        PageRequest pageRequest;
        if (sort.equals(EventSort.EVENT_DATE)) {
            pageRequest = PageRequest.of(from / size, size, Sort.by("eventDate"));
        } else {
            pageRequest = PageRequest.of(from / size, size, Sort.unsorted());
        }
        List<Event> events = eventRepository.findByFiltersPublic(text, categories, paid, rangeStart, rangeEnd, EventState.PUBLISHED, pageRequest);

        if (onlyAvailable) {
            events = events.stream()
                    .filter(event -> event.getParticipantLimit() == 0
                            || event.getParticipantLimit() < requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED))
                    .collect(Collectors.toList());
        }
        Map<Long, Long> eventRequests = requestRepository.findAllByEventInAndStatus(events, RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.groupingBy(participationRequest -> participationRequest.getEvent().getId(), Collectors.counting()));
        if (!eventRequests.isEmpty()) {
            for (Event event : events) {
                event.setConfirmedRequests(eventRequests.getOrDefault(event.getId(), 0L));
            }
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        ResponseEntity<Object> views = statsClient.getStats(rangeStart, rangeEnd, uris, true);
        ObjectMapper objectMapper = new ObjectMapper();
        List<ViewStatsDto> viewStatsList = objectMapper.convertValue(views.getBody(), new TypeReference<>() {
        });

        List<EventShortDto> result = events.stream()
                .map(EventMapper::toEventShortDto)
                .peek(eventFullDto -> {
                    Optional<ViewStatsDto> viewStatsDto = viewStatsList.stream()
                            .filter(viewStatsDto1 -> viewStatsDto1.getUri().equals("/events/" + eventFullDto.getId()))
                            .findFirst();
                    eventFullDto.setViews(viewStatsDto.map(ViewStatsDto::getHits).orElse(0L));
                }).peek(eventFullDto -> eventFullDto.setConfirmedRequests(
                        requestRepository.countByEventIdAndStatus(eventFullDto.getId(), RequestStatus.CONFIRMED)))
                .collect(Collectors.toList());

        log.info("Найдено событий: {}", result.size());

        if (sort.equals(EventSort.VIEWS)) {
            return result.stream()
                    .sorted(Comparator.comparingLong(EventShortDto::getViews))
                    .collect(Collectors.toList());
        }
        return result;
    }

    @Override
    @Transactional
    public EventFullDto getEventPublic(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(String.format("Событие с id%d не найдено.", eventId)));
        event.setConfirmedRequests(requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new EventNotFoundException("Событие должно быть опубликовано.");
        }
        LocalDateTime startTime = event.getCreatedOn();
        LocalDateTime endTime = LocalDateTime.now();
        List<String> uris = List.of("/events/" + event.getId());

        ResponseEntity<Object> views = statsClient.getStats(startTime, endTime, uris, true);
        ObjectMapper objectMapper = new ObjectMapper();

        if (views.getStatusCode().is2xxSuccessful()) {
            List<ViewStatsDto> viewStatsList = objectMapper.convertValue(views.getBody(), new TypeReference<>() {
            });
            event.setViews(viewStatsList.isEmpty() ? 0L : viewStatsList.get(0).getHits());
        }

        log.info("Получено событие с id{}", event.getId());

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public void createStats(HttpServletRequest request) {
        String app = "main-service";
        statsClient.save(EndpointHitDto.builder()
                .app(app)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Event getEventByInitiatorAndEventId(Long userId, Long eventId) {
        getUser(userId);
        return eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new EventNotFoundException(String.format("Событие с id%d у пользователя с id%d не найдено.", eventId, userId)));
    }

    private void isEventEarlier(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new WrongTimeException("Событие не может быть раньше, чем через два часа от текущего момента.");
        }
    }
}
