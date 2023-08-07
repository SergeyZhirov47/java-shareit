package ru.practicum.shareit.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.DAOUser;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


//@ContextConfiguration(classes = RepositoryConfig.class)
// @Import(UserRepository.class)
//@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))

//@DataJpaTest
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))

// @SpringBootTest
//@DataJpaTest
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
// @DataJpaTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserRepositoryTest {
    private final DAOUser userRepository;
    //private final UserRepository userRepository;
    private TestEntityManager em;

    @Test
    public void existsByEmail_whenUserExists_thenTrue() {
        final String userEmail = "userEmail@somemail.com";
        final User user = User.builder()
                .name("John Doe")
                .email(userEmail)
                .build();
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail(userEmail));
    }

    @Test
    public void existsByEmail_whenUserNotExists_thenFalse() {
        final List<User> users = userRepository.findAll();
        assertTrue(users.isEmpty());

        assertFalse(userRepository.existsByEmail("anyUserEmail@userEmail.com"));
    }

    @Test
    public void getUserById_whenUserExists_thenOk() {
        User user = User.builder()
                .name("John Doe")
                .email("userEmail@somemail.com")
                .build();
        user = userRepository.save(user);
        final long userId = user.getId();

        final User userFromDb = userRepository.getUserById(userId);
        assertEquals(userId, userFromDb.getId());
        assertEquals(user, userFromDb);
    }

    @Test
    public void getUserById_whenUserNotExists_thenThrowException() {
        final List<User> users = userRepository.findAll();
        assertTrue(users.isEmpty());

        final long notExistedUserId = 9999L;
        assertThrows(UserNotFoundException.class, () -> userRepository.getUserById(notExistedUserId));
    }

    @Test
    public void checkUserExists_whenUserExists_thenOk() {
        User user = User.builder()
                .name("John Doe")
                .email("userEmail@somemail.com")
                .build();
        user = userRepository.save(user);
        final long userId = user.getId();

        assertDoesNotThrow(() -> userRepository.checkUserExists(userId));
    }

    @Test
    public void checkUserExists_whenUserNotExists_thenThrowException() {
        final List<User> users = userRepository.findAll();
        assertTrue(users.isEmpty());

        final long notExistedUserId = 9999L;
        assertThrows(UserNotFoundException.class, () -> userRepository.checkUserExists(notExistedUserId));
    }

    @AfterEach
    public void clearAllUsers() {
        userRepository.deleteAll();
    }
}
