package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

        // ✅ Универсальный способ получения ID
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa() != null ? film.getMpa().getId() : 1);
            return ps;
        }, keyHolder);

        Number id = keyHolder.getKey();
        if (id != null) {
            film.setId(id.intValue());
        }

        log.debug("Фильм создан: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : 1,
                film.getId());

        if (rowsUpdated == 0) {
            log.warn("Фильм не найден для обновления: {}", film.getId());
            return null;
        }
        log.debug("Фильм обновлён: {}", film);
        return film;
    }

    @Override
    public void delete(Integer id) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
        log.debug("Фильм удалён: {}", id);
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbcTemplate.query("SELECT * FROM films", (rs, rowNum) -> {
            Film f = new Film();
            f.setId(rs.getInt("id"));
            f.setName(rs.getString("name"));
            f.setDescription(rs.getString("description"));
            f.setReleaseDate(rs.getDate("release_date").toLocalDate());
            f.setDuration(rs.getInt("duration"));

            Integer mpaId = rs.getInt("mpa_id");
            if (!rs.wasNull()) {
                Mpa mpa = new Mpa();
                mpa.setId(mpaId);
                f.setMpa(mpa);
            }
            return f;
        });

        films.forEach(this::loadFilmDetails);
        return films;
    }

    @Override
    public Optional<Film> findById(Integer id) {
        try {
            Film film = jdbcTemplate.queryForObject("SELECT * FROM films WHERE id = ?", (rs, rowNum) -> {
                Film f = new Film();
                f.setId(rs.getInt("id"));
                f.setName(rs.getString("name"));
                f.setDescription(rs.getString("description"));
                f.setReleaseDate(rs.getDate("release_date").toLocalDate());
                f.setDuration(rs.getInt("duration"));

                Integer mpaId = rs.getInt("mpa_id");
                if (!rs.wasNull()) {
                    Mpa mpa = new Mpa();
                    mpa.setId(mpaId);
                    f.setMpa(mpa);
                }
                return f;
            }, id);

            if (film != null) {
                loadFilmDetails(film);
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // --- Методы для работы с лайками и популярными фильмами (требуются сервису) ---

    public void addLike(Integer filmId, Integer userId) {
        // MERGE INTO гарантирует, что дубликат не будет создан
        String sql = "MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        log.debug("Добавлен лайк: film {} <- user {}", filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
        log.debug("Удален лайк: film {} <- user {}", filmId, userId);
    }

    public List<Film> getPopular(Integer count) {
        String sql = "SELECT f.* FROM films f " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(l.user_id) DESC, f.id ASC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film f = new Film();
            f.setId(rs.getInt("id"));
            f.setName(rs.getString("name"));
            f.setDescription(rs.getString("description"));
            f.setReleaseDate(rs.getDate("release_date").toLocalDate());
            f.setDuration(rs.getInt("duration"));

            Integer mpaId = rs.getInt("mpa_id");
            if (!rs.wasNull()) {
                Mpa mpa = new Mpa();
                mpa.setId(mpaId);
                f.setMpa(mpa);
            }
            return f;
        }, count);

        films.forEach(this::loadFilmDetails);
        return films;
    }

    private void loadFilmDetails(Film film) {
        // 1. Загружаем MPA
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            film.setMpa(jdbcTemplate.queryForObject("SELECT * FROM mpa WHERE id = ?", (rs, rowNum) -> {
                Mpa m = new Mpa();
                m.setId(rs.getInt("id"));
                m.setName(rs.getString("name"));
                m.setDescription(rs.getString("description"));
                return m;
            }, film.getMpa().getId()));
        }

        // 2. Загружаем Жанры
        String genreSql = "SELECT g.* FROM genres g JOIN film_genre fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
        film.setGenres(new HashSet<>(jdbcTemplate.query(genreSql, (rs, rowNum) -> {
            Genre g = new Genre();
            g.setId(rs.getInt("id"));
            g.setName(rs.getString("name"));
            return g;
        }, film.getId())));

        // 3. 🔥 ДОБАВЛЯЕМ ЗАГРУЗКУ ЛАЙКОВ (этого не хватало) 🔥
        String likesSql = "SELECT user_id FROM likes WHERE film_id = ?";
        Set<Integer> likes = new HashSet<>(jdbcTemplate.queryForList(likesSql, Integer.class, film.getId()));
        film.setLikes(likes);
    }
    }
