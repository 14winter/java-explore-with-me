package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto create(NewCategoryDto newCategoryDto);

    CategoryDto update(Long catId, NewCategoryDto newCategoryDto);

    void delete(Long catId);

    CategoryDto getCategoryByID(Long catId);

    List<CategoryDto> getCategories(int from, int size);
}
