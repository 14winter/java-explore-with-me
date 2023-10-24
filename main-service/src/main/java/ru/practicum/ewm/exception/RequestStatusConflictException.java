package ru.practicum.ewm.exception;

public class RequestStatusConflictException extends RuntimeException {
    public RequestStatusConflictException(String message) {
        super(message);
    }
}
