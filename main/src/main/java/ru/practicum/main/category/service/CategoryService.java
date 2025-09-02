package ru.practicum.main.category.service;

import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    // Админские методы
    CategoryDto createCategory(NewCategoryDto dto);

    CategoryDto updateCategory(Long id, NewCategoryDto dto);

    void deleteCategory(Long id);

    // Публичные методы
    List<CategoryDto> getAllCategories(int from, int size);

    CategoryDto getCategoryById(Long id);
    //TODO IF NEEDED разделить на разные сервисы
}
