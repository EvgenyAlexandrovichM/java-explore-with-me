package ru.practicum.stats.dto.mapper;

import org.mapstruct.Mapper;

import ru.practicum.stats.dto.EndPointHitCreateDto;
import ru.practicum.stats.dto.EndPointHitResponseDto;
import ru.practicum.stats.entity.EndpointHit;

@Mapper(componentModel = "spring")
public interface StatsMapper {

    EndpointHit toEntity(EndPointHitCreateDto dto);

    EndPointHitResponseDto toDto(EndpointHit entity);
}
