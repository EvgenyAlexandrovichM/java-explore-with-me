package ru.practicum.emw.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.main.user.entity.User;
import ru.practicum.main.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    void existsByEmail_shouldReturnTrue_whenUserWithEmailExists() {
        User user = new User(null, "Ivan", "ivan@yandex.ru");
        repository.save(user);

        boolean exists = repository.existsByEmail("ivan@yandex.ru");

        assertTrue(exists);
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenUserWithEmailDoesNotExists() {
        User user = new User(null, "Ivan", "ivan@yandex.ru");
        repository.save(user);

        boolean exists = repository.existsByEmail("andrey@yandex.ru");

        assertFalse(exists);
    }
}


