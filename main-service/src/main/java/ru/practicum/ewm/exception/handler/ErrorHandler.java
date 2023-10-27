package ru.practicum.ewm.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.exception.*;

@Slf4j
@RestControllerAdvice("ru.practicum.ewm")
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handle(final CategoryNotFoundException e) {
        log.error("CategoryNotFoundException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(e.getMessage())
                .reason("Category not found")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handle(final UserNotFoundException e) {
        log.error("UserNotFoundException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(e.getMessage())
                .reason("User not found")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handle(final EventNotFoundException e) {
        log.error("EventNotFoundException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(e.getMessage())
                .reason("Event not found")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handle(final ParticipationRequestNotFoundException e) {
        log.error("ParticipationRequestNotFoundException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(e.getMessage())
                .reason("ParticipationRequest not found")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handle(final CompilationNotFoundException e) {
        log.error("CompilationNotFoundException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(e.getMessage())
                .reason("Compilation not found")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handle(final CommentNotFoundException e) {
        log.error("CommentNotFoundException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(e.getMessage())
                .reason("Comment not found")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(final CategoryAlreadyExistException e) {
        log.error("CategoryAlreadyExistException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .message(e.getMessage())
                .reason("Category already exist")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(final UserAlreadyExistException e) {
        log.error("UserAlreadyExistException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .message(e.getMessage())
                .reason("User already exist")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(final ParticipationRequestAlreadyExistException e) {
        log.error("ParticipationRequestAlreadyExistException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .message(e.getMessage())
                .reason("Participation request already exist")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(final RequestStatusConflictException e) {
        log.error("RequestStatusConflictException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .message(e.getMessage())
                .reason("Request status conflict")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(final EventAlreadyPublishedException e) {
        log.error("EventAlreadyPublishedException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .message(e.getMessage())
                .reason("Event already published")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(final EventStateConflictException e) {
        log.error("EventStateConflictException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .message(e.getMessage())
                .reason("Event state conflict")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(final EventNotPublishedException e) {
        log.error("EventNotPublishedException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .message(e.getMessage())
                .reason("Event not published")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(final ParticipantLimitException e) {
        log.error("ParticipantLimitException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .message(e.getMessage())
                .reason("Participant limit exception")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(final UserEventException e) {
        log.error("UserEventException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .message(e.getMessage())
                .reason("User event")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(final CommentConflictException e) {
        log.error("CommentConflictException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .message(e.getMessage())
                .reason("Comment conflict")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handle(final WrongTimeException e) {
        log.error("WrongTimeException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(e.getMessage())
                .reason("Wrong time")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handle(final IncorrectParameterException e) {
        log.error("IncorrectParameterException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(e.getMessage())
                .reason("Incorrect parameter")
                .build();
    }
}
