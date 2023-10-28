package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.NewCommentDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {
    CommentDto create(Long userId, NewCommentDto newCommentDto);

    List<CommentDto> getComments(Long userId, int from, int size);

    CommentDto updatePrivate(Long userId, Long commentId, NewCommentDto dto);

    void deletePrivate(Long userId, Long commentId);

    List<CommentDto> search(List<Long> users, List<Long> events, LocalDateTime rangeStart, LocalDateTime rangeEnd, String text, int from, int size);

    void deleteAdmin(Long commentId);

    List<CommentDto> getCommentsToEvent(Long userId, int from, int size);
}