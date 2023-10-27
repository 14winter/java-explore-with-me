package ru.practicum.ewm.exception;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(Long commentId) {
        super(String.format("Комментарий с id%d не найден.", commentId));
    }
}
