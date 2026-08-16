package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MpaDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public Collection<Mpa> findAll() {
        String sql = "SELECT * FROM mpa ORDER BY id";
        return jdbcTemplate.query(sql, this::mapRowToMpa);
    }

    public Optional<Mpa> findById(Integer id) {
        String sql = "SELECT * FROM mpa WHERE id = ?";
        try {
            Mpa mpa = jdbcTemplate.queryForObject(sql, this::mapRowToMpa, id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Mpa mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("id"));
        mpa.setName(rs.getString("name"));
        mpa.setDescription(rs.getString("description"));
        return mpa;
    }
}