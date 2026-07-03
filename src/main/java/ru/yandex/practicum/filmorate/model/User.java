package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import java.time.LocalDate;

@Data
public class User {
    private int id;

    @Email(message = "Электронная почта не может быть пустой и должна содержать символ @")
    @NotBlank(message = "Электронная почта не может быть пустой")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    // Проверка на пробелы реализуется вручную или через @Pattern,
    // но в базовом спринте часто достаточно ручной проверки в контроллере/сервисе,
    // однако для чистоты кода оставим поле, а проверку сделаем в логике создания.
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}