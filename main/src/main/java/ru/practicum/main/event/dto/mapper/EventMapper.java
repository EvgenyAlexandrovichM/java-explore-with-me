package ru.practicum.main.event.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.main.category.dto.mapper.CategoryMapper;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.dto.NewEventDto;
import ru.practicum.main.event.entity.Event;
import ru.practicum.main.event.entity.location.Location;
import ru.practicum.main.event.dto.location.LocationDto;
import ru.practicum.main.user.dto.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface EventMapper {

    EventShortDto toShortDto(Event event);

    EventFullDto toFullDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "requests", ignore = true)
    @Mapping(target = "participantLimit", ignore = true)
    @Mapping(target = "requestModeration", ignore = true)
    @Mapping(target = "location", source = "location", qualifiedByName = "toLocation")
    Event fromNewEventDto(NewEventDto dto);

    @Named("toLocation")
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
