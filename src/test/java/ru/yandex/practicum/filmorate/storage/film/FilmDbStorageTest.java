package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    private Film createTestFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(90);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);
        return filmDbStorage.create(film);
    }

    @Test
    void create_shouldSaveFilmAndGenerateId() {
        Film film = createTestFilm("Test Film");
        assertThat(film.getId()).isNotNull();
        assertThat(film.getName()).isEqualTo("Test Film");
    }

    @Test
    void update_shouldUpdateFilm() {
        Film film = createTestFilm("Old Name");
        film.setName("New Name");
        film.setDuration(120);

        Film updated = filmDbStorage.update(film);
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getDuration()).isEqualTo(120);
    }

    @Test
    void delete_shouldRemoveFilm() {
        Film film = createTestFilm("To Delete");
        filmDbStorage.delete(film.getId());

        Optional<Film> found = filmDbStorage.findById(film.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllFilms() {
        createTestFilm("Film 1");
        createTestFilm("Film 2");

        Collection<Film> films = filmDbStorage.findAll();
        assertThat(films).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void findById_shouldReturnFilmWhenExists() {
        Film film = createTestFilm("Find Me");

        Optional<Film> found = filmDbStorage.findById(film.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Find Me");
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        Optional<Film> found = filmDbStorage.findById(9999);
        assertThat(found).isEmpty();
    }

    @Test
    void addLike_and_removeLike_shouldWorkCorrectly() {
        Film film = createTestFilm("Liked Film");
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("test");
        user.setName("Test");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user = userDbStorage.create(user);

        // Добавляем лайк
        filmDbStorage.addLike(film.getId(), user.getId());

        // Проверяем, что лайк добавился (через getPopular или findById)
        Film filmWithLike = filmDbStorage.findById(film.getId()).orElseThrow();
        assertThat(filmWithLike.getLikes()).contains(user.getId());

        // Удаляем лайк
        filmDbStorage.removeLike(film.getId(), user.getId());

        Film filmWithoutLike = filmDbStorage.findById(film.getId()).orElseThrow();
        assertThat(filmWithoutLike.getLikes()).doesNotContain(user.getId());
    }

    @Test
    void getPopular_shouldReturnFilmsOrderedByLikes() {
        Film film1 = createTestFilm("Popular Film");
        Film film2 = createTestFilm("Not So Popular Film");

        User user1 = userDbStorage.create(createTestUser("user1"));
        User user2 = userDbStorage.create(createTestUser("user2"));

        // Film 1 получает 2 лайка, Film 2 получает 1 лайк
        filmDbStorage.addLike(film1.getId(), user1.getId());
        filmDbStorage.addLike(film1.getId(), user2.getId());
        filmDbStorage.addLike(film2.getId(), user1.getId());

        List<Film> popular = filmDbStorage.getPopular(10);

        assertThat(popular).isNotEmpty();
        assertThat(popular.get(0).getId()).isEqualTo(film1.getId());
        assertThat(popular.get(1).getId()).isEqualTo(film2.getId());
    }

    private User createTestUser(String login) {
        User user = new User();
        user.setEmail(login + "@test.com");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}