package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FriendAndLikeServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private FilmService filmService;

    private User user1, user2, user3;
    private Film film1, film2;

    @BeforeEach
    void setUp() {
        // Создаем пользователей
        user1 = createUser("user1", "User One", "user1@test.com");
        user2 = createUser("user2", "User Two", "user2@test.com");
        user3 = createUser("user3", "User Three", "user3@test.com");

        // Создаем фильмы
        film1 = createFilm("Film 1", "Desc 1");
        film2 = createFilm("Film 2", "Desc 2");
    }

    @Test
    void addFriend_shouldAddOneWayFriendship() {
        // user1 добавляет user2 в друзья (односторонняя дружба)
        userService.addFriend(user1.getId(), user2.getId());

        Collection<User> friendsOf1 = userService.getFriends(user1.getId());
        Collection<User> friendsOf2 = userService.getFriends(user2.getId());

        // У user1 в друзьях должен быть user2
        assertTrue(friendsOf1.stream().anyMatch(u -> u.getId().equals(user2.getId())),
                "user2 должен быть в списке друзей у user1");

        // У user2 в друзьях НЕ должно быть user1, так как он не добавлял его в ответ
        assertFalse(friendsOf2.stream().anyMatch(u -> u.getId().equals(user1.getId())),
                "user1 НЕ должен быть в списке друзей у user2 при односторонней заявке");
    }

    @Test
    void removeFriend_shouldRemoveMutualFriendship() {
        userService.addFriend(user1.getId(), user2.getId());
        userService.removeFriend(user1.getId(), user2.getId());

        Collection<User> friendsOf1 = userService.getFriends(user1.getId());
        Collection<User> friendsOf2 = userService.getFriends(user2.getId());

        assertFalse(friendsOf1.stream().anyMatch(u -> u.getId().equals(user2.getId())));
        assertFalse(friendsOf2.stream().anyMatch(u -> u.getId().equals(user1.getId())));
    }

    @Test
    void getCommonFriends_shouldReturnOnlyCommonFriends() {
        // user1 дружит с user2 и user3
        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user3.getId());
        // user2 дружит только с user3 (и user1, но это не важно для пересечения с user3)
        userService.addFriend(user2.getId(), user3.getId());

        Collection<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertTrue(commonFriends.stream().anyMatch(u -> u.getId().equals(user3.getId())));
    }

    @Test
    void addAndRemoveLike_shouldUpdateFilmLikes() {
        filmService.addLike(film1.getId(), user1.getId());
        filmService.addLike(film1.getId(), user2.getId());
        filmService.addLike(film2.getId(), user1.getId());

        List<Film> popular = filmService.getPopular(10);

        // film1 должен быть первым (2 лайка), film2 вторым (1 лайк)
        assertEquals(film1.getId(), popular.get(0).getId());
        assertEquals(2, popular.get(0).getLikes().size());
        assertEquals(film2.getId(), popular.get(1).getId());
        assertEquals(1, popular.get(1).getLikes().size());

        // Удаляем лайк user1 у film1
        filmService.removeLike(film1.getId(), user1.getId());

        Film updatedFilm1 = filmService.findById(film1.getId());
        assertEquals(1, updatedFilm1.getLikes().size());
        assertFalse(updatedFilm1.getLikes().contains(user1.getId()));
    }

    @Test
    void updateFilm_shouldPreserveLikes() {
        filmService.addLike(film1.getId(), user1.getId());
        filmService.addLike(film1.getId(), user2.getId());

        // Обновляем только название и описание
        Film updateRequest = new Film();
        updateRequest.setId(film1.getId());
        updateRequest.setName("Updated Film Name");
        updateRequest.setDescription("Updated Desc");
        updateRequest.setReleaseDate(LocalDate.of(2020, 1, 1));
        updateRequest.setDuration(120);

        Film updatedFilm = filmService.update(updateRequest);

        assertEquals("Updated Film Name", updatedFilm.getName());
        // ✅ Ключевая проверка: лайки не должны были стереться
        assertEquals(2, updatedFilm.getLikes().size());
        assertTrue(updatedFilm.getLikes().contains(user1.getId()));
        assertTrue(updatedFilm.getLikes().contains(user2.getId()));
    }

    @Test
    void updateUser_shouldPreserveFriends() {
        userService.addFriend(user1.getId(), user2.getId());

        // Обновляем только имя и email
        User updateRequest = new User();
        updateRequest.setId(user1.getId());
        updateRequest.setName("New Name");
        updateRequest.setEmail("newemail@test.com");
        updateRequest.setLogin("newlogin");
        updateRequest.setBirthday(LocalDate.of(1990, 1, 1));

        User updatedUser = userService.update(updateRequest);

        assertEquals("New Name", updatedUser.getName());
        // ✅ Ключевая проверка: друзья не должны были стереться
        assertEquals(1, updatedUser.getFriends().size());
        assertTrue(updatedUser.getFriends().contains(user2.getId()));
    }

    @Test
    void addLikeToUnknownUser_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> {
            filmService.addLike(film1.getId(), 999);
        });
    }

    @Test
    void removeLikeFromUnknownUser_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> {
            filmService.removeLike(film1.getId(), 999);
        });
    }

    // Вспомогательные методы
    private User createUser(String login, String name, String email) {
        User user = new User();
        user.setLogin(login);
        user.setName(name);
        user.setEmail(email);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userService.create(user);
    }

    private Film createFilm(String name, String description) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        return filmService.create(film);
    }
}