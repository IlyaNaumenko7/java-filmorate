package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;

    public Film create(Film film) {
        validateFilm(film);
        if (film.getMpa() != null) {
            mpaDbStorage.findById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("MPA с id " + film.getMpa().getId() + " не найден"));
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreDbStorage.findById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id " + genre.getId() + " не найден"));
            }
        }
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        Film existingFilm = findById(film.getId());
        existingFilm.setName(film.getName());
        existingFilm.setDescription(film.getDescription());
        existingFilm.setReleaseDate(film.getReleaseDate());
        existingFilm.setDuration(film.getDuration());

        if (film.getMpa() != null) {
            mpaDbStorage.findById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("MPA с id " + film.getMpa().getId() + " не найден"));
            existingFilm.setMpa(film.getMpa());
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreDbStorage.findById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id " + genre.getId() + " не найден"));
            }
            existingFilm.setGenres(film.getGenres());
        }

        filmStorage.update(existingFilm);
        return findById(film.getId());
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Integer id) {
        return filmStorage.findById(id).orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public void addLike(Integer filmId, Integer userId) {
        findById(filmId);
        userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден")
        );
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        findById(filmId);
        userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден")
        );
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopular(Integer count) {
        return filmStorage.getPopular(count);
    }

    private void validateFilm(Film film) {
        if (film == null) throw new ValidationException("Тело запроса не может быть пустым");
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}