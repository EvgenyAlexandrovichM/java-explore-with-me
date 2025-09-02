package ru.practicum.main.compilation.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.main.compilation.entity.Compilation;
import ru.practicum.main.event.dto.mapper.EventMapper;

@Mapper(componentModel = "spring", uses = EventMapper.class)
public interface CompilationMapper {

    @Mapping(target = "events", ignore = true)
    Compilation toEntity(NewCompilationDto dto);

    @Mapping(target = "events", ignore = true)
    Compilation toEntity(UpdateCompilationRequest dto);

    CompilationDto toDto(Compilation compilation);
}
