package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;

    // Новые поля по ТЗ
    private Mpa mpa;
    private Set<Genre> genres = new HashSet<>();

    // Для обратной совместимости со старыми тестами (можно убрать при полном переходе на БД)
    private Set<Integer> likes = new HashSet<>();
}