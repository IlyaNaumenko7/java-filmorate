package ru.yandex.practicum.filmorate.storage.genre;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreDbStorage;

    @Test
    void findAll_shouldReturnAllGenres() {
        Collection<Genre> genres = genreDbStorage.findAll();
        assertThat(genres).hasSize(6);
    }

    @Test
    void findById_shouldReturnGenreWhenExists() {
        Optional<Genre> genre = genreDbStorage.findById(1);
        assertThat(genre).isPresent();
        assertThat(genre.get().getName()).isEqualTo("Комедия");
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        Optional<Genre> genre = genreDbStorage.findById(999);
        assertThat(genre).isEmpty();
    }
}