package ru.practicum.shareit.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.DaoUser;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private DaoUser daoUser;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void create() {
        final UserCreateDto userCreateDto = UserCreateDto.builder()
                .name("User")
                .email("user@someEmail.com")
                .build();

        final long userId = 1L;
        final User newUser = UserMapper.toUser(userCreateDto);
        newUser.setId(userId);

        Mockito.when(daoUser.save(any(User.class))).thenReturn(newUser);

        final long userIdFromService = userService.create(userCreateDto);

        assertEquals(userId, userIdFromService);
        verify(daoUser).save(any(User.class));
    }

    @Test
    public void createAndGet() {
        final UserCreateDto userCreateDto = UserCreateDto.builder()
                .name("User")
                .email("user@someEmail.com")
                .build();

        final long userId = 1L;
        final User newUser = UserMapper.toUser(userCreateDto);
        newUser.setId(userId);

        Mockito.when(daoUser.save(any(User.class))).thenReturn(newUser);
        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(newUser);

        final UserDto newUserDto = userService.createAndGet(userCreateDto);

        assertEquals(userId, newUserDto.getId());
        assertEquals(newUser.getName(), newUserDto.getName());
        assertEquals(newUser.getEmail(), newUserDto.getEmail());
        verify(daoUser).save(any(User.class));
        verify(daoUser).getUserById(anyLong());
    }

    @Test
    public void getById() {

    }

    @Test
    public void getAll() {

    }

    @Test
    public void update() {

    }

    @Test
    public void delete() {

    }
}
