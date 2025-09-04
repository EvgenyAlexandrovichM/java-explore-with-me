package ru.practicum.main.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;
import ru.practicum.main.category.dto.mapper.CategoryMapper;
import ru.practicum.main.category.entity.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.EntityAlreadyExistsException;
import ru.practicum.main.exception.EntityNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper mapper;

    @Override
    public CategoryDto createCategory(NewCategoryDto dto) {
        ensureUniqueName(dto.getName());
        Category category = mapper.fromNewDto(dto);
        Category saved = categoryRepository.save(category);
        log.info("CategoryId={} saved successfully", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    public CategoryDto updateCategory(Long id, NewCategoryDto dto) {
        Category category = getCategoryOrThrow(id);

        String newName = dto.getName();
        if (newName != null && !newName.equals(category.getName())) {
            ensureUniqueName(newName);
            category.setName(newName);
        }

        Category updated = categoryRepository.save(category);
        log.info("CategoryId={} updated successfully", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    public void deleteCategory(Long id) {
        getCategoryOrThrow(id);
        existsByCategoryId(id);
        categoryRepository.deleteById(id);
        log.info("CategoryId={} deleted", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories(int from, int size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size))
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        Category category = getCategoryOrThrow(id);
        log.info("Getting category by id={}", id);
        return mapper.toDto(category);
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("CategoryId={} not found", id);
                    return new EntityNotFoundException("Category with id " + id + " not found");
                });
    }

    private void existsByCategoryId(Long id) {
        if (eventRepository.existsByCategoryId(id)) {
            log.warn("Cannot delete categoryId={} its linked to eventId", id);
            throw new ConflictException(
                    "Cannot delete category with id " + id + " because it is linked to existing events");
        }
    }

    private void ensureUniqueName(String name) {
        if (categoryRepository.existsByName(name)) {
            log.warn("Category name={} already exists", name);
            throw new EntityAlreadyExistsException("Category with name " + name + " already exists");
        }
    }
}
