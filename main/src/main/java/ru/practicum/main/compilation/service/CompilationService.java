package ru.practicum.main.compilation.service;

import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    //Админские методы

    CompilationDto createCompilation(NewCompilationDto dto);

    CompilationDto updateCompilation(Long id, UpdateCompilationRequest dto);

    void deleteCompilation(Long id);

    //Публичные методы

    CompilationDto getCompilationById(Long id);

    List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size);
    //TODO IF NEEDED разделить на разные сервисы
}
