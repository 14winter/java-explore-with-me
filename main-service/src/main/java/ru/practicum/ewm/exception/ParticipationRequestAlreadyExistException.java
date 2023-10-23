package ru.practicum.ewm.exception;

public class ParticipationRequestAlreadyExistException extends RuntimeException {
    public ParticipationRequestAlreadyExistException(String message) {
        super(message);
    }
}
