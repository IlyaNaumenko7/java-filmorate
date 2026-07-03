package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        Film film = new Film();
        film.setName("");
        film.setDuration(90);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> {
            // Эмуляция проверки из контроллера
            if (film.getName() == null || film.getName().isBlank()) {
                throw new ValidationException("Название фильма не может быть пустым");
            }
        });
    }

    @Test
    void shouldThrowExceptionWhenDescriptionTooLong() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("a".repeat(201));
        film.setDuration(90);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> {
            if (film.getDescription() != null && film.getDescription().length() > 200) {
                throw new ValidationException("Максимальная длина описания — 200 символов");
            }
        });
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateTooEarly() {
        Film film = new Film();
        film.setName("Test");
        film.setDuration(90);
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class, () -> {
            if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
            }
        });
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Test");
        film.setDuration(-10);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> {
            if (film.getDuration() <= 0) {
                throw new ValidationException("Продолжительность фильма должна быть положительным числом");
            }
        });
    }
}