package ru.practicum.shareit.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.DaoUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserRepositoryTest {
    private final DaoUser daoUser;
    private TestEntityManager em;

    @Test
    public void existsByEmail_whenUserExists_thenTrue() {
        final String userEmail = "userEmail@somemail.com";
        final User user = User.builder()
                .name("John Doe")
                .email(userEmail)
                .build();
        daoUser.save(user);

        assertTrue(daoUser.existsByEmail(userEmail));
    }

    @Test
    public void existsByEmail_whenUserNotExists_thenFalse() {
        final List<User> users = daoUser.findAll();
        assertTrue(users.isEmpty());

        assertFalse(daoUser.existsByEmail("anyUserEmail@userEmail.com"));
    }

    @Test
    public void getUserById_whenUserExists_thenOk() {
        User user = User.builder()
                .name("John Doe")
                .email("userEmail@somemail.com")
                .build();
        user = daoUser.save(user);
        final long userId = user.getId();

        final User userFromDb = daoUser.getUserById(userId);
        assertEquals(userId, userFromDb.getId());
        assertEquals(user, userFromDb);
    }

    @Test
    public void getUserById_whenUserNotExists_thenThrowException() {
        final List<User> users = daoUser.findAll();
        assertTrue(users.isEmpty());

        final long notExistedUserId = 9999L;
        assertThrows(UserNotFoundException.class, () -> daoUser.getUserById(notExistedUserId));
    }

    @Test
    public void checkUserExists_whenUserExists_thenOk() {
        User user = User.builder()
                .name("John Doe")
                .email("userEmail@somemail.com")
                .build();
        user = daoUser.save(user);
        final long userId = user.getId();

        assertDoesNotThrow(() -> daoUser.checkUserExists(userId));
    }

    @Test
    public void checkUserExists_whenUserNotExists_thenThrowException() {
        final List<User> users = daoUser.findAll();
        assertTrue(users.isEmpty());

        final long notExistedUserId = 9999L;
        assertThrows(UserNotFoundException.class, () -> daoUser.checkUserExists(notExistedUserId));
    }

    @AfterEach
    public void clearAllUsers() {
        daoUser.deleteAll();
    }
}
