package ru.practicum.main.event.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.main.category.dto.mapper.CategoryMapper;
import ru.practicum.main.event.dto.EventDto;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.dto.NewEventDto;
import ru.practicum.main.event.entity.Event;
import ru.practicum.main.event.location.Location;
import ru.practicum.main.event.location.LocationDto;
import ru.practicum.main.user.dto.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface EventMapper {

    EventShortDto toShortDto(Event event);

    EventDto toDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "category", ignore = true)
    Event fromNewEventDto(NewEventDto dto);

    default Location toLocation(LocationDto dto) {
        if (dto == null) return null;
        return Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    default LocationDto toLocationDto(Location location) {
        if (location == null) return null;
        return LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();


    }

}
