package ru.practicum.stats.dto.mapper;

import org.mapstruct.Mapper;

import ru.practicum.stats.dto.EndpointHitCreateDto;
import ru.practicum.stats.dto.EndpointHitResponseDto;

import ru.practicum.stats.entity.EndpointHit;

@Mapper(componentModel = "spring")
public interface StatsMapper {

    EndpointHit toEntity(EndpointHitCreateDto dto);

    EndpointHitResponseDto toDto(EndpointHit entity);
}
