package ru.practicum.emw.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.main.exception.EntityAlreadyExistsException;
import ru.practicum.main.exception.EntityNotFoundException;
import ru.practicum.main.user.dto.NewUserRequest;
import ru.practicum.main.user.dto.UserDto;
import ru.practicum.main.user.dto.mapper.UserMapper;
import ru.practicum.main.user.entity.User;
import ru.practicum.main.user.repository.UserRepository;
import ru.practicum.main.user.repository.UserServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository repository;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserServiceImpl service;

    private NewUserRequest request;
    private User user1;
    private User user2;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        request = new NewUserRequest("Ivan", "ivan@yandex.ru");
        user1 = new User(1L, "Ivan", "ivan@yandex.ru");
        user2 = new User(2L, "Andrey", "andrey@yandex.ru");
        userDto = new UserDto(1L, "Ivan", "ivan@yandex.ru");
    }

    @Test
    void createUser_shouldSaveAndReturnDto_whenEmailNotExists() {
        when(repository.existsByEmail(request.getEmail())).thenReturn(false);
        when(mapper.fromNewUserRequest(request)).thenReturn(user1);
        when(mapper.toDto(user1)).thenReturn(userDto);
        when(repository.save(user1)).thenReturn(user1);

        UserDto result = service.createUser(request);

        assertEquals(userDto, result);
        verify(repository).save(user1);
    }

    @Test
    void createUser_shouldThrow_whenEmailExists() {
        when(repository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class,
                () -> service.createUser(request)
        );
        verify(repository, never()).save(any());
    }

    @Test
    void getUsers_shouldReturnPagedList_whenNoIdsProvided() {
        PageRequest page = PageRequest.of(0, 2);
        List<User> users = List.of(
                user1,
                user2
        );
        when(repository.findAll(page)).thenReturn(new PageImpl<>(users));
        when(mapper.toDto(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return new UserDto(u.getId(), u.getName(), u.getEmail());
        });

        List<UserDto> result = service.getUsers(null, 0, 2);

        assertEquals(2, result.size());
        assertEquals("Ivan", result.getFirst().getName());
    }

    @Test
    void getUsers_shouldReturnByIds_whenIdsProvided() {
        List<Long> ids = List.of(1L, 2L);
        List<User> users = List.of(
                user1,
                user2
        );
        when(repository.findAllById(ids)).thenReturn(users);
        when(mapper.toDto(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return new UserDto(u.getId(), u.getName(), u.getEmail());
        });

        List<UserDto> result = service.getUsers(ids, 0, 10);

        assertEquals(2, result.size());
        verify(repository, never()).findAll(any(PageRequest.class));
    }

    @Test
    void deleteUser_shouldDelete_whenExists() {

        when(repository.existsById(1L)).thenReturn(true);

        service.deleteUser(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteUser_shouldThrow_whenNotExists() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> service.deleteUser(1L)
        );

        verify(repository, never()).deleteById(any());
    }
}
