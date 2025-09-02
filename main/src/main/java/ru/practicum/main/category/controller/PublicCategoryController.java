package ru.practicum.main.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
public class PublicCategoryController {

    private final CategoryService service;

    @GetMapping
    public List<CategoryDto> getAllCategories(@RequestParam(defaultValue = "0") int from,
                                              @RequestParam(defaultValue = "10") int size) {
        log.info("GET: Calling endpoint /categories - from={}, size={}", from, size);
        return service.getAllCategories(from, size);
    }

    @GetMapping("/{id}")
    private CategoryDto getCategoryById(@PathVariable Long id) {
        log.info("GET: Calling endpoint /categories/{}", id);
        return service.getCategoryById(id);
    }
}
