package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User create(User user) {
        validateUser(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        validateUser(user);
        if (userStorage.findById(user.getId()).isEmpty()) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        return userStorage.update(user);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Integer id) {
        return userStorage.findById(id).orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = findById(userId);
        User friend = findById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId); // Взаимная дружба
        userStorage.update(user);
        userStorage.update(friend);
        System.out.println("Пользователь " + user.getLogin() + " добавил в друзья " + friend.getLogin());
    }

    public void removeFriend(Integer userId, Integer friendId) {
        User user = findById(userId);
        User friend = findById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId); // Взаимное удаление
        userStorage.update(user);
        userStorage.update(friend);
    }

    public Collection<User> getFriends(Integer userId) {
        User user = findById(userId);
        return user.getFriends().stream()
                .map(this::findById)
                .collect(Collectors.toList());
   }

    public Collection<User> getCommonFriends(Integer userId, Integer otherId) {
        User user = findById(userId);
        User other = findById(otherId);

        return user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .map(this::findById)
                .collect(Collectors.toList());
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