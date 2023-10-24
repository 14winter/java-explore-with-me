package ru.practicum.ewm.exception;

public class CompilationNotFoundException extends RuntimeException {
    public CompilationNotFoundException(Long id) {
        super(String.format("Подборка событий с id%d не найдена.", id));
    }
}
