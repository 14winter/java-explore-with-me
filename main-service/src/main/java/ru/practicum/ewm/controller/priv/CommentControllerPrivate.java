package ru.practicum.ewm.controller.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.service.CommentService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/comments")
public class CommentControllerPrivate {
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@PathVariable Long userId, @Valid @RequestBody NewCommentDto newCommentDto) {
        log.info("Получен POST запрос от пользователя с id{} к users/{userId}/comments: {}", userId, newCommentDto);
        return commentService.create(userId, newCommentDto);
    }

    @GetMapping
    public List<CommentDto> getComments(@PathVariable Long userId,
                                        @RequestParam(defaultValue = "0") int from,
                                        @RequestParam(defaultValue = "10") int size) {
        log.info("Получен Get запрос к /users/{userId}/comments от пользователя с id{}", userId);
        return commentService.getComments(userId, from, size);
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(@PathVariable Long userId, @PathVariable Long commentId, @Valid @RequestBody NewCommentDto newCommentDto) {
        log.info("Получен Patch запрос от пользователя с id{} к users/{userId}/comments/{commentId}: commentId{}", userId, commentId);
        return commentService.updatePrivate(userId, commentId, newCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId, @PathVariable Long commentId) {
        log.info("Получен Delete запрос к /users/{userId}/comments/{commentId}: commentId{}", commentId);
        commentService.deletePrivate(userId, commentId);
    }
}
