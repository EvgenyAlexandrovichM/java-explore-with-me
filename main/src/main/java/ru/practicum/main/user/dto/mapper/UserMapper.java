package ru.practicum.main.user.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.main.user.dto.NewUserRequest;
import ru.practicum.main.user.dto.UserDto;
import ru.practicum.main.user.dto.UserShortDto;
import ru.practicum.main.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    UserShortDto toShortDto(User user);

    @Mapping(target = "id", ignore = true)
    User fromNewUserRequest(NewUserRequest dto);
}
