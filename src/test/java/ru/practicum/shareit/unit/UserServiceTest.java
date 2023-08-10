package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeAll;
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
import ru.practicum.shareit.user.service.UserServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    private static UserCreateDto userCreateDto;
    private static User newUser;
    @Mock
    private DaoUser daoUser;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeAll
    public static void beforeAllInit() {
        userCreateDto = UserCreateDto.builder()
                .name("User")
                .email("user@someEmail.com")
                .build();

        final long userId = 1L;
        newUser = UserMapper.toUser(userCreateDto);
        newUser.setId(userId);
    }

    @Test
    public void create_whenValidUser_thenReturnId() {
        Mockito.when(daoUser.save(any(User.class))).thenReturn(newUser);

        final long userIdFromService = userService.create(userCreateDto);

        assertEquals(newUser.getId(), userIdFromService);
        verify(daoUser).save(any(User.class));
    }

    @Test
    public void createAndGet() {
        Mockito.when(daoUser.save(any(User.class))).thenReturn(newUser);
        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(newUser);

        final UserDto newUserDto = userService.createAndGet(userCreateDto);

        assertEquals(newUser.getId(), newUserDto.getId());
        assertEquals(newUser.getName(), newUserDto.getName());
        assertEquals(newUser.getEmail(), newUserDto.getEmail());
        verify(daoUser).save(any(User.class));
        verify(daoUser).getUserById(anyLong());
    }

    @Test
    public void update_whenUpdateOk_thenReturnUserDto() {
        final UserDto userDtoWithChanges = UserDto.builder()
                .name("new name")
                .email("newEmail@email.org")
                .build();
        final User userWithChanges = UserMapper.toUser(userDtoWithChanges);

        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(newUser);
        Mockito.when(daoUser.save(any(User.class))).thenReturn(userWithChanges);

        final UserDto updatedDUserDto = userService.update(newUser.getId(), userDtoWithChanges);

        assertNotNull(updatedDUserDto);
        assertEquals(newUser.getId(), updatedDUserDto.getId());
        assertEquals(userDtoWithChanges.getName(), updatedDUserDto.getName());
        assertEquals(userDtoWithChanges.getEmail(), updatedDUserDto.getEmail());

        verify(daoUser).getUserById(anyLong());
        verify(daoUser).save(any(User.class));
    }

    @Test
    public void update_whenNothingUpdate_thenReturnUserDto() {
        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(newUser);

        final UserDto sameUserDto = UserDto.builder()
                .name(userCreateDto.getName())
                .email(userCreateDto.getEmail())
                .build();

        final UserDto updatedDUserDto = userService.update(newUser.getId(), sameUserDto);

        assertNotNull(updatedDUserDto);
        assertEquals(newUser.getId(), updatedDUserDto.getId());
        assertEquals(sameUserDto.getName(), updatedDUserDto.getName());
        assertEquals(sameUserDto.getEmail(), updatedDUserDto.getEmail());

        verify(daoUser).getUserById(anyLong());
        verify(daoUser, never()).save(any(User.class));
    }

    @Test
    public void update_whenOnlyName_thenReturnUserDto() {
        /*
        final UserDto userDtoWithChanges = UserDto.builder()
                .name("new name")
                .build();
        final User userWithChanges = UserMapper.toUser(userDtoWithChanges);

        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(newUser);
        Mockito.when(daoUser.save(any(User.class))).thenReturn(userWithChanges);

        final UserDto updatedDUserDto = userService.update(newUser.getId(), userDtoWithChanges);

        assertNotNull(updatedDUserDto);
        assertEquals(newUser.getId(), updatedDUserDto.getId());
        assertEquals(userDtoWithChanges.getName(), updatedDUserDto.getName());
        assertEquals(newUser.getEmail(), updatedDUserDto.getEmail());

        verify(daoUser).getUserById(anyLong());
        verify(daoUser).save(any(User.class));

         */
    }

    @Test
    public void getById_when_then() {

    }

    @Test
    public void getAll_when_then() {

    }

    @Test
    public void delete_when_then() {

    }
}
