package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDbStorage userStorage;

    public User create(User user) {
        validateUser(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        User existingUser = findById(user.getId());

        if (user.getEmail() != null) existingUser.setEmail(user.getEmail());
        if (user.getLogin() != null) existingUser.setLogin(user.getLogin());
        if (user.getName() != null) existingUser.setName(user.getName());
        if (user.getBirthday() != null) existingUser.setBirthday(user.getBirthday());

        userStorage.update(existingUser);

        // Перезагружаем пользователя из БД, чтобы вернуть его с актуальным списком друзей
        return findById(user.getId());
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Integer id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public void addFriend(Integer userId, Integer friendId) {
        findById(userId);
        findById(friendId);
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        findById(userId);
        findById(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public Collection<User> getFriends(Integer userId) {
        findById(userId);
        return userStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(Integer userId, Integer otherId) {
        findById(userId);
        findById(otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}