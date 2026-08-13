package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // Гарантирует откат всех изменений в БД после завершения каждого теста
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userDbStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate; // Добавляем для очистки таблицы

    private User testUser;

    @BeforeEach
    void setUp() {
        // Очищаем таблицу перед каждым тестом, чтобы убрать данные,
        // которые могли остаться от других тестовых классов
        jdbcTemplate.update("DELETE FROM users");

        testUser = new User();
        testUser.setEmail("test@test.com");
        testUser.setLogin("testLogin");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void create_shouldSaveUserAndGenerateId() {
        User created = userDbStorage.create(testUser);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(created.getLogin()).isEqualTo(testUser.getLogin());
    }

    @Test
    void findById_shouldReturnUserWhenExists() {
        User created = userDbStorage.create(testUser);

        Optional<User> found = userDbStorage.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        Optional<User> found = userDbStorage.findById(999);
        assertThat(found).isEmpty();
    }

    @Test
    void update_shouldUpdateUser() {
        User created = userDbStorage.create(testUser);
        created.setName("Updated Name");

        User updated = userDbStorage.update(created);
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void delete_shouldRemoveUser() {
        User created = userDbStorage.create(testUser);
        userDbStorage.delete(created.getId());

        Optional<User> found = userDbStorage.findById(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        userDbStorage.create(testUser);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2Login");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1995, 5, 15));
        userDbStorage.create(user2);

        Collection<User> allUsers = userDbStorage.findAll();

        // Теперь размер будет ровно 2, так как мы очистили таблицу в @BeforeEach
        assertThat(allUsers).hasSize(2);
    }
}