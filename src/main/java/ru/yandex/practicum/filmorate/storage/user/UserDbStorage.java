package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null);
            return ps;
        }, keyHolder);
        Number id = keyHolder.getKey();
        if (id != null) {
            user.setId(id.intValue());
        }
        log.debug("Пользователь создан: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                user.getId());
        if (rowsUpdated == 0) {
            log.warn("Пользователь не найден для обновления: {}", user.getId());
            return null;
        }
        log.debug("Пользователь обновлён: {}", user);
        return user;
    }

    @Override
    public void delete(Integer id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
        log.debug("Пользователь удалён: {}", id);
    }

    @Override
    public Collection<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users", (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setEmail(rs.getString("email"));
            u.setLogin(rs.getString("login"));
            u.setName(rs.getString("name"));
            u.setBirthday(rs.getDate("birthday") != null ? rs.getDate("birthday").toLocalDate() : null);
            return u;
        });
    }

    @Override
    public Optional<User> findById(Integer id) {
        try {
            User user = jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?", (rs, rowNum) -> {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setLogin(rs.getString("login"));
                u.setName(rs.getString("name"));
                u.setBirthday(rs.getDate("birthday") != null ? rs.getDate("birthday").toLocalDate() : null);
                return u;
            }, id);
            if (user != null) {
                String friendsSql = "SELECT friend_id FROM friendship WHERE user_id = ?";
                List<Integer> friendIds = jdbcTemplate.queryForList(friendsSql, Integer.class, id);
                user.setFriends(new HashSet<>(friendIds));
            }
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // ⚠️ ОДНОСТОРОННЯЯ ДРУЖБА - только userId добавляет friendId
    public void addFriend(Integer userId, Integer friendId) {
        String sql = "MERGE INTO friendship (user_id, friend_id) KEY (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    // ⚠️ ОДНОСТОРОННЕЕ УДАЛЕНИЕ
    public void removeFriend(Integer userId, Integer friendId) {
        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    public List<User> getFriends(Integer userId) {
        String sql = "SELECT u.* FROM users u JOIN friendship f ON u.id = f.friend_id WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setEmail(rs.getString("email"));
            u.setLogin(rs.getString("login"));
            u.setName(rs.getString("name"));
            u.setBirthday(rs.getDate("birthday") != null ? rs.getDate("birthday").toLocalDate() : null);
            return u;
        }, userId);
    }

    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendship f1 ON u.id = f1.friend_id AND f1.user_id = ? " +
                "JOIN friendship f2 ON u.id = f2.friend_id AND f2.user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setEmail(rs.getString("email"));
            u.setLogin(rs.getString("login"));
            u.setName(rs.getString("name"));
            u.setBirthday(rs.getDate("birthday") != null ? rs.getDate("birthday").toLocalDate() : null);
            return u;
        }, userId, otherId);
    }
}