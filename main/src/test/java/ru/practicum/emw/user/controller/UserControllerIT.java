package ru.practicum.emw.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.exception.EntityAlreadyExistsException;
import ru.practicum.main.exception.EntityNotFoundException;
import ru.practicum.main.user.dto.NewUserRequest;
import ru.practicum.main.user.dto.UserDto;
import ru.practicum.main.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService service;

    private NewUserRequest request;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        request = new NewUserRequest("Ivan", "ivan@yandex.ru");
        userDto = new UserDto(1L, "Ivan", "ivan@yandex.ru");
    }

    @Test
    void createUser_shouldReturn201() throws Exception {
        given(service.createUser(any(NewUserRequest.class)))
                .willReturn(userDto);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@yandex.ru"));
    }

    @Test
    void createUser_withInvalidEmail_returns400() throws Exception {
        NewUserRequest badInput = new NewUserRequest("Ivan", "ivan.yandex.ru");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(badInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Validation failed"));
    }

    @Test
    void createUser_withDuplicateEmail_returns400() throws Exception {
        given(service.createUser(any(NewUserRequest.class)))
                .willThrow(new EntityAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void getUsers_withoutIds_returnsPageList() throws Exception {
        given(service.getUsers(null, 0, 10))
                .willReturn(List.of(userDto));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void deleteUser_existingId_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/admin/users/{id}", 1L))
                .andExpect(status().isNoContent());

        then(service).should(times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_notFound_returns404() throws Exception {
        willThrow(new EntityNotFoundException("User not found"))
                .given(service).deleteUser(999L);

        mockMvc.perform(delete("/admin/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
