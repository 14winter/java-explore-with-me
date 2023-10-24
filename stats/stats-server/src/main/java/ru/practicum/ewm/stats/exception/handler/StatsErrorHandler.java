package ru.practicum.ewm.stats.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.stats.exception.WrongTimeException;

@Slf4j
@RestControllerAdvice("ru.practicum.ewm.stats")
public class StatsErrorHandler {

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
}
