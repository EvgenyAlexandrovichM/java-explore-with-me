package ru.practicum.main.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.main.compilation.dto.mapper.CompilationMapper;
import ru.practicum.main.compilation.entity.Compilation;
import ru.practicum.main.compilation.repository.CompilationRepository;
import ru.practicum.main.event.entity.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.EntityNotFoundException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper mapper;

    @Override
    public CompilationDto createCompilation(NewCompilationDto dto) {
        Compilation compilation = mapper.toEntity(dto);

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            compilation.setEvents(loadEvents(dto.getEvents()));
        } else {
            compilation.setEvents(Collections.emptySet());
        }
        Compilation saved = compilationRepository.save(compilation);
        log.info("CompilationId={} saved successfully", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    public CompilationDto updateCompilation(Long id, UpdateCompilationRequest dto) {
        Compilation compilation = getCompilationOrThrow(id);
        updatedBasicFields(compilation, dto);
        log.info("Basic fields updated for compilation id={}", compilation.getId());
        return mapper.toDto(compilationRepository.save(compilation));
    }

    @Override
    public void deleteCompilation(Long id) {
        getCompilationOrThrow(id);
        compilationRepository.deleteById(id);
        log.info("CompilationId={} deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long id) {
       Compilation compilation = getCompilationOrThrow(id);
       return mapper.toDto(compilation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        Page<Compilation> compilationPage;
        if (pinned != null) {
            compilationPage = compilationRepository.findAllByPinned(pinned, pageable);
        } else {
            compilationPage = compilationRepository.findAll(pageable);
        }
        return compilationPage.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    private HashSet<Event> loadEvents(List<Long> ids) {
        return new HashSet<>(eventRepository.findAllById(ids));
    }

    private Compilation getCompilationOrThrow(Long id) {
        return compilationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("CompilationId={} not found", id);
                    return new EntityNotFoundException("Compilation with id " + id + " not found");
                });
    }

    private void updatedBasicFields(Compilation compilation, UpdateCompilationRequest dto) {
        if (dto.getTitle() != null) compilation.setTitle(dto.getTitle());
        if (dto.getPinned() != null) compilation.setPinned(dto.getPinned());
        if (dto.getEvents() != null) compilation.setEvents(loadEvents(dto.getEvents()));
    }
}
