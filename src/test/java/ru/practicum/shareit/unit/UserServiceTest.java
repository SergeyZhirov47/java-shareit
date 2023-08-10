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
import ru.practicum.shareit.user.exception.EmailAlreadyUsedException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.DaoUser;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
        final User userForMock = UserMapper.toUser(userDtoWithChanges);
        userForMock.setId(newUser.getId());

        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(newUser);
        Mockito.when(daoUser.save(any(User.class))).thenReturn(userForMock);

        final UserDto updatedUserDto = userService.update(newUser.getId(), userDtoWithChanges);

        assertNotNull(updatedUserDto);
        assertEquals(newUser.getId(), updatedUserDto.getId());
        assertEquals(userDtoWithChanges.getName(), updatedUserDto.getName());
        assertEquals(userDtoWithChanges.getEmail(), updatedUserDto.getEmail());

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
        final UserDto userDtoWithChanges = UserDto.builder()
                .name("new name")
                .build();
        final User userForMock = User.builder()
                .id(newUser.getId())
                .name(userDtoWithChanges.getName())
                .email(newUser.getEmail())
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(newUser);
        Mockito.when(daoUser.save(any(User.class))).thenReturn(userForMock);

        final UserDto updatedDUserDto = userService.update(newUser.getId(), userDtoWithChanges);

        assertNotNull(updatedDUserDto);
        assertEquals(userForMock.getId(), updatedDUserDto.getId());
        assertEquals(userDtoWithChanges.getName(), updatedDUserDto.getName());
        assertEquals(userForMock.getEmail(), updatedDUserDto.getEmail());

        verify(daoUser).getUserById(anyLong());
        verify(daoUser).save(any(User.class));
    }

    @Test
    public void update_whenOnlyEmail_thenReturnUserDto() {
        final UserDto userDtoWithChanges = UserDto.builder()
                .email("newEmail.com")
                .build();
        final User userForMock = User.builder()
                .id(newUser.getId())
                .name(newUser.getName())
                .email(userDtoWithChanges.getEmail())
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(newUser);
        Mockito.when(daoUser.save(any(User.class))).thenReturn(userForMock);

        final UserDto updatedDUserDto = userService.update(newUser.getId(), userDtoWithChanges);

        assertNotNull(updatedDUserDto);
        assertEquals(userForMock.getId(), updatedDUserDto.getId());
        assertEquals(userForMock.getName(), updatedDUserDto.getName());
        assertEquals(userDtoWithChanges.getEmail(), updatedDUserDto.getEmail());

        verify(daoUser).getUserById(anyLong());
        verify(daoUser).save(any(User.class));
    }

    @Test
    public void update_whenEmailRepeat_thenThrowException() {
        final UserDto userDtoWithChanges = UserDto.builder()
                .name("new Name")
                .email("alreadyUserEmail@email.com")
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(newUser);
        Mockito.when(daoUser.existsByEmail(anyString())).thenReturn(true);

        assertThrows(EmailAlreadyUsedException.class, () -> userService.update(newUser.getId(), userDtoWithChanges));

        verify(daoUser).getUserById(anyLong());
        verify(daoUser, never()).save(any(User.class));
    }

    @Test
    public void getAll_whenOk_thenReturnAll() {
        final List<User> allUsersList = List.of(newUser);
        Mockito.when(daoUser.findAll()).thenReturn(allUsersList);

        final List<UserDto> allUsers = userService.getAll();
        assertFalse(allUsers.isEmpty());
        assertEquals(1, allUsers.size());
        assertEquals(UserMapper.toUserDto(newUser), allUsers.get(0));

        verify(daoUser).findAll();
    }

    @Test
    public void delete() {
        doNothing().when(daoUser).deleteById(anyLong());

        userService.delete(anyLong());

        verify(daoUser).deleteById(anyLong());
    }
}
