package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailAlreadyUsedException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceIT {
    private final UserService userService;
    private final UserRepository userRepository;

    // Проверки на то как БД контролирует уникальность email.
    @Test
    public void createAndGet_whenNoUsers_whenCreate() {
        val userCreateDto = UserCreateDto.builder()
                .name("user")
                .email("user@email.com")
                .build();

        assertEquals(0, userRepository.count());
        val newUserDto = userService.createAndGet(userCreateDto);

        assertEquals(1, userRepository.count());
        assertEquals(userCreateDto.getName(), newUserDto.getName());
        assertEquals(userCreateDto.getEmail(), newUserDto.getEmail());
    }

    @Test
    public void createAndGet_whenHasUserButUniqueEmail_whenCreate() {
        assertEquals(0, userRepository.count());

        val userCreateDto1 = UserCreateDto.builder()
                .name("user 1")
                .email("user1@email.com")
                .build();

        val userCreateDto2 = UserCreateDto.builder()
                .name("user 2")
                .email("user2@email.com")
                .build();

        val newUserDto1 = userService.createAndGet(userCreateDto1);
        val newUserDto2 = userService.createAndGet(userCreateDto2);

        assertEquals(2, userRepository.count());
        assertEquals(userCreateDto1.getEmail(), newUserDto1.getEmail());
        assertEquals(userCreateDto2.getEmail(), newUserDto2.getEmail());
    }

    @Test
    public void createAndGet_whenHasUserButDuplicateEmail_whenThrowException() {
        assertEquals(0, userRepository.count());

        val userCreateDto1 = UserCreateDto.builder()
                .name("user 1")
                .email("user1@email.com")
                .build();
        val newUserDto1 = userService.createAndGet(userCreateDto1);

        val duplicateEmailCreateDto = UserCreateDto.builder()
                .name("user 2")
                .email(userCreateDto1.getEmail())
                .build();

        assertThrows(EmailAlreadyUsedException.class, () -> userService.createAndGet(duplicateEmailCreateDto));
        assertEquals(1, userRepository.count());
    }

    @AfterEach
    public void clean() {
        userRepository.deleteAll();
    }

    @Nested
    public class TestUpdateMethod {
        private UserDto user;

        @BeforeEach
        public void init() {
            val userCreateDto = UserCreateDto.builder()
                    .name("user")
                    .email("user@email.com")
                    .build();

            user = userService.createAndGet(userCreateDto);
        }

        @Test
        public void update_whenOneUser_whenOk() {
            val newEmail = "newEmail@email.com";
            val userDto = UserDto.builder()
                    .name("new name")
                    .email(newEmail)
                    .build();

            assertEquals(1, userRepository.count());
            assertFalse(userRepository.existsByEmail(newEmail));

            final long userId = user.getId();
            val updatedUser = userService.update(userId, userDto);

            assertEquals(1, userRepository.count());
            assertEquals(userId, updatedUser.getId());
            assertEquals(userDto.getName(), updatedUser.getName());
            assertEquals(userDto.getEmail(), updatedUser.getEmail());

            val userFromDB = userRepository.findById(userId).get();
            assertEquals(userFromDB.getId(), updatedUser.getId());
            assertEquals(userFromDB.getName(), updatedUser.getName());
            assertEquals(userFromDB.getEmail(), updatedUser.getEmail());
        }

        @Test
        public void update_whenNoUser_whenThrowException() {
            val userDto = UserDto.builder()
                    .name("new name")
                    .email("newEmail@email.com")
                    .build();

            userRepository.deleteById(user.getId());

            assertThrows(UserNotFoundException.class, () -> userService.update(user.getId(), userDto));
        }

        @Test
        public void update_whenHasUserButUniqueEmail_whenOk() {
            val newUser = UserCreateDto.builder()
                    .name("user2")
                    .email("user2@email.com")
                    .build();
            val user2 = userService.createAndGet(newUser);

            val newEmail = "newEmail@email.com";
            assertEquals(2, userRepository.count());
            assertFalse(userRepository.existsByEmail(newEmail));

            val forUpdateDto = UserDto.builder()
                    .name("new name")
                    .email("newEmail@email.com")
                    .build();

            final long userId = user.getId();
            val updatedUser = userService.update(userId, forUpdateDto);

            assertEquals(2, userRepository.count());
            assertEquals(userId, updatedUser.getId());
            assertEquals(forUpdateDto.getName(), updatedUser.getName());
            assertEquals(forUpdateDto.getEmail(), updatedUser.getEmail());
        }

        @Test
        public void update_whenHasUserButSameEmail_whenOk() {
            assertEquals(1, userRepository.count());

            val forUpdateDto = UserDto.builder()
                    .name("new name")
                    .email(user.getEmail())
                    .build();

            final long userId = user.getId();
            val updatedUser = userService.update(userId, forUpdateDto);

            assertEquals(1, userRepository.count());
            assertEquals(userId, updatedUser.getId());
            assertEquals(forUpdateDto.getName(), updatedUser.getName());
            assertEquals(forUpdateDto.getEmail(), updatedUser.getEmail());
        }

        @Test
        public void update_whenHasUserButDuplicateEmail_whenOk() {
            val newUser = UserCreateDto.builder()
                    .name("user2")
                    .email("user2@email.com")
                    .build();
            val user2 = userService.createAndGet(newUser);

            assertEquals(2, userRepository.count());

            val forUpdateDto = UserDto.builder()
                    .name("new name for user")
                    .email(newUser.getEmail())
                    .build();

            val userId = user.getId();
            val oldUserEmail = user.getEmail();
            assertThrows(EmailAlreadyUsedException.class, () -> userService.update(userId, forUpdateDto));
            user = userService.getById(userId);

            assertEquals(2, userRepository.count());
            assertEquals(newUser.getEmail(), user2.getEmail());
            assertEquals(oldUserEmail, user.getEmail()); // у кого пытались обновить почту она осталась прежней
        }
    }
}
