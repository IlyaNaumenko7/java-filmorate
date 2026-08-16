package ru.yandex.practicum.filmorate.storage.mpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaDbStorage;

    @Test
    void findAll_shouldReturnAllMpas() {
        Collection<Mpa> mpas = mpaDbStorage.findAll();
        assertThat(mpas).hasSize(5);
    }

    @Test
    void findById_shouldReturnMpaWhenExists() {
        Optional<Mpa> mpa = mpaDbStorage.findById(1);
        assertThat(mpa).isPresent();
        assertThat(mpa.get().getName()).isEqualTo("G");
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        Optional<Mpa> mpa = mpaDbStorage.findById(999);
        assertThat(mpa).isEmpty();
    }
}