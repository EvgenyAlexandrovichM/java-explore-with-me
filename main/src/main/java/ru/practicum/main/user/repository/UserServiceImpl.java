package ru.practicum.main.user.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.exception.EntityAlreadyExistsException;
import ru.practicum.main.exception.EntityNotFoundException;
import ru.practicum.main.user.dto.NewUserRequest;
import ru.practicum.main.user.dto.UserDto;
import ru.practicum.main.user.dto.mapper.UserMapper;
import ru.practicum.main.user.entity.User;
import ru.practicum.main.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            log.warn("User email={} already exists", dto.getEmail());
            throw new EntityAlreadyExistsException("User " + dto + " with email " + dto.getEmail() + "already exists");
        }
        User user = mapper.fromNewUserRequest(dto);
        repository.save(user);
        log.info("User with id={} saved successfully", user.getId());
        return mapper.toDto(user);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        if (ids != null && !ids.isEmpty()) {
            return repository.findAllById(ids).stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
        } else {
            return repository.findAll(pageRequest).stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!repository.existsById(userId)) {
            log.warn("User with id={} not found", userId);
            throw new EntityNotFoundException("User with id " + userId + " not found");
        }
        repository.deleteById(userId);
        log.info("User with id={} deleted successfully", userId);
    }
}
