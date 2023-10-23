package ru.practicum.ewm.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long id) {
        super(String.format("Категория с id%d не найдена.", id));
    }
}
