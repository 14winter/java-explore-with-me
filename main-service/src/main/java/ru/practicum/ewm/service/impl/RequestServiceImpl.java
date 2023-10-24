package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.enums.EventState;
import ru.practicum.ewm.enums.RequestStatus;
import ru.practicum.ewm.exception.*;
import ru.practicum.ewm.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.RequestService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User user = getUser(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(String.format("Событие с id%d не найдено.", eventId)));

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ParticipationRequestAlreadyExistException("Нельзя добавить повторный запрос.");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new UserEventException("Инициатор события не может добавить запрос на участие в своём событии.");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new EventNotPublishedException("Нельзя участвовать в неопубликованном событии.");
        }
        if (event.getParticipantLimit() > 0) {
            if (event.getParticipantLimit() <= requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)) {
                throw new ParticipantLimitException("Достигнут лимит по заявкам на данное событие.");
            }
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTimestamp = LocalDateTime.now().format(formatter);
        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.parse(formattedTimestamp, formatter))
                .event(event)
                .requester(user)
                .status(event.getRequestModeration() && event.getParticipantLimit() != 0 ? RequestStatus.PENDING : RequestStatus.CONFIRMED)
                .build();

        ParticipationRequest createdRequest = requestRepository.save(request);
        log.info("Добавлен запрос на участие с id{}", createdRequest.getId());
        return ParticipationRequestMapper.toParticipationRequestDto(createdRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequests(Long userId) {
        getUser(userId);
        List<ParticipationRequest> requests = requestRepository.findAllByRequesterId(userId);
        log.info("Получено запросов на участие: {}", requests.size());
        return requests.stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        getUser(userId);
        ParticipationRequest participationRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new ParticipationRequestNotFoundException(String.format("Запрос на участие с id%d не найден.", requestId)));
        participationRequest.setStatus(RequestStatus.CANCELED);

        ParticipationRequest cancelledParticipationRequest = requestRepository.save(participationRequest);
        log.debug("Запрос на участие с id{} отменено", cancelledParticipationRequest.getId());
        return ParticipationRequestMapper.toParticipationRequestDto(cancelledParticipationRequest);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
