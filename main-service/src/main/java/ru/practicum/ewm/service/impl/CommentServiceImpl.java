package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.exception.*;
import ru.practicum.ewm.mapper.CommentMapper;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.CommentRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CommentDto create(Long userId, NewCommentDto newCommentDto) {
        User author = getUser(userId);
        Event event = getEvent(newCommentDto.getEventId());
        Comment comment = CommentMapper.toComment(newCommentDto);
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());
        Comment createdComment = commentRepository.save(comment);
        log.info("Добавлен комментарий с id{}", createdComment.getId());
        return CommentMapper.toCommentDto(createdComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(Long userId, int from, int size) {
        getUser(userId);
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("created"));
        List<Comment> comments = commentRepository.findAllByAuthorId(userId, pageRequest);
        log.info("Получено комментариев: {}", comments.size());
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto updatePrivate(Long userId, Long commentId, NewCommentDto newCommentDto) {
        getUser(userId);
        Comment oldComment = getComment(commentId);
        if (!oldComment.getAuthor().getId().equals(userId)) {
            throw new CommentConflictException("Комментарий можно изменить, только являясь его автором.");
        }
        if (oldComment.getCreated().plusMinutes(10).isBefore(LocalDateTime.now())) {
            throw new WrongTimeException("Комментарий можно изменить, только в течении десяти минут с момента публикации.");
        }
        oldComment.setText(newCommentDto.getText());
        Comment updatedComment = commentRepository.save(oldComment);
        log.info("Комментарий с id{} обновлен", updatedComment.getId());
        return CommentMapper.toCommentDto(updatedComment);
    }

    @Override
    @Transactional
    public void deletePrivate(Long userId, Long commentId) {
        getUser(userId);
        Comment comment = getComment(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new CommentConflictException("Комментарий можно удалить, только являясь его автором.");
        }
        commentRepository.deleteById(commentId);
        log.info("Комментарий с id{} удален", commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> search(List<Long> users, List<Long> events, LocalDateTime rangeStart, LocalDateTime rangeEnd, String text, int from, int size) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd)) {
                throw new IncorrectParameterException("Некорректные параметры даты.");
            }
        }
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("created"));
        List<Comment> comments = commentRepository.findByFilters(text, users, events, rangeStart, rangeEnd, pageRequest);
        List<CommentDto> result = comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        log.info("Найдено комментариев: {}", result.size());
        return result;
    }

    @Override
    @Transactional
    public void deleteAdmin(Long commentId) {
        getComment(commentId);
        commentRepository.deleteById(commentId);
        log.info("Комментарий с id{} удален", commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsToEvent(Long eventId, int from, int size) {
        getEvent(eventId);
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("created"));
        List<Comment> comments = commentRepository.findByEventId(eventId, pageRequest);
        log.info("Получено комментариев: {}", comments.size());
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(String.format("Событие с id%d не найдено.", eventId)));
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
    }
}
