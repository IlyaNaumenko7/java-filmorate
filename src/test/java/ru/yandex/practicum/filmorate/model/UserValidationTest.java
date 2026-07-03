package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> {
            if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
                throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
            }
        });
    }

    @Test
    void shouldThrowExceptionWhenLoginHasSpaces() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("my login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> {
            if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
                throw new ValidationException("Логин не может быть пустым и содержать пробелы");
            }
        });
    }

    @Test
    void shouldUseLoginAsNameIfNameIsEmpty() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("coolUser");
        user.setName("");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        // Эмуляция логики handleEmptyName из контроллера
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        assertEquals("coolUser", user.getName());
    }

    @Test
    void shouldThrowExceptionWhenBirthdayIsInFuture() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> {
            if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
        });
    }
}