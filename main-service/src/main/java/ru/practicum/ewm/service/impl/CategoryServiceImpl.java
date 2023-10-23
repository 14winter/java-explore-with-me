package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.exception.CategoryAlreadyExistException;
import ru.practicum.ewm.exception.CategoryNotFoundException;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        isExistByName(newCategoryDto.getName());
        Category createdCategory = categoryRepository.save(CategoryMapper.toCategory(newCategoryDto));
        log.info("Добавлена категория {}", createdCategory);
        return CategoryMapper.toCategoryDto(createdCategory);
    }

    @Override
    @Transactional
    public CategoryDto update(Long catId, NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            Category category = categoryRepository.findByName(newCategoryDto.getName());
            if (!category.getId().equals(catId)) {
                throw new CategoryAlreadyExistException("Категория с именем '" + newCategoryDto.getName() + "' уже существует.");
            }
        }
        Category oldCategory = getCategory(catId);
        oldCategory.setName(newCategoryDto.getName());
        Category updatedCategory = categoryRepository.save(oldCategory);
        log.debug("Категория с id{} обновлена", updatedCategory.getId());
        return CategoryMapper.toCategoryDto(updatedCategory);
    }

    @Override
    @Transactional
    public void delete(Long catId) {
        getCategory(catId);
        if (eventRepository.existsByCategoryId(catId)) {
            throw new CategoryAlreadyExistException("Нельзя удалить категорию со связанным событием");
        }
        categoryRepository.deleteById(catId);
        log.debug("Категория с id{} удалена", catId);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryByID(Long catId) {
        Category category = getCategory(catId);
        log.info("Получена категория с id{}", category.getId());
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Category> categories = categoryRepository.findAll(pageRequest).toList();
        log.info("Получено категорий: {}", categories.size());
        return categories.stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    public Category getCategory(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException(catId));
    }

    private void isExistByName(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new CategoryAlreadyExistException("Категория с именем '" + name + "' уже существует.");
        }
    }
}
