package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailAlreadyUsedException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserControllerTest {
    private static final String BASE_ENDPOINT = "/users";
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    @MockBean
    private final UserService userService;
    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("John Doe")
            .email("johnDoe@somemail.com")
            .build();

    @SneakyThrows
    @Test
    public void create() {
        final UserCreateDto correctUser = UserCreateDto.builder()
                .name("John Doe")
                .email("johnDoe@somemail.com")
                .build();

        Mockito.when(userService.createAndGet(correctUser)).thenReturn(userDto);

        mockMvc.perform(post(BASE_ENDPOINT)
                        .content(objectMapper.writeValueAsBytes(correctUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        verify(userService).createAndGet(correctUser);
    }

    @SneakyThrows
    @Test
    public void update_whenUpdateWhole_thenReturnUser() {
        final long userId = 1L;
        final UserDto updatedUser = UserDto.builder()
                .name("new John Doe")
                .email("newJohnDoe@somemail.com")
                .build();
        final UserDto returnedUser = UserDto.builder()
                .id(userId)
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .build();

        Mockito.when(userService.update(userId, updatedUser)).thenReturn(returnedUser);

        mockMvc.perform(patch(BASE_ENDPOINT + "/{id}", userId)
                        .content(objectMapper.writeValueAsBytes(updatedUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(returnedUser.getId()))
                .andExpect(jsonPath("$.name").value(returnedUser.getName()))
                .andExpect(jsonPath("$.email").value(returnedUser.getEmail()));
    }

    @SneakyThrows
    @Test
    public void update_whenNameOnly_thenReturnUser() {
        final long userId = 1L;
        final UserDto updatedUser = UserDto.builder()
                .name("new John Doe")
                .build();
        final UserDto returnedUser = UserDto.builder()
                .id(userId)
                .name(updatedUser.getName())
                .email("JohnDoe@somemail.com")
                .build();

        Mockito.when(userService.update(userId, updatedUser)).thenReturn(returnedUser);

        mockMvc.perform(patch(BASE_ENDPOINT + "/{id}", userId)
                        .content(objectMapper.writeValueAsBytes(updatedUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(returnedUser.getId()))
                .andExpect(jsonPath("$.name").value(returnedUser.getName()))
                .andExpect(jsonPath("$.email").value(returnedUser.getEmail()));
    }

    @SneakyThrows
    @Test
    public void update_whenEmailOnly_thenReturnUser() {
        final long userId = 1L;
        final UserDto updatedUser = UserDto.builder()
                .email("newJohnDoe@somemail.com")
                .build();
        final UserDto returnedUser = UserDto.builder()
                .id(userId)
                .name("John Doe")
                .email(updatedUser.getEmail())
                .build();

        Mockito.when(userService.update(userId, updatedUser)).thenReturn(returnedUser);

        mockMvc.perform(patch(BASE_ENDPOINT + "/{id}", userId)
                        .content(objectMapper.writeValueAsBytes(updatedUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(returnedUser.getId()))
                .andExpect(jsonPath("$.name").value(returnedUser.getName()))
                .andExpect(jsonPath("$.email").value(returnedUser.getEmail()));
    }

    @SneakyThrows
    @Test
    public void update_whenEmailAlreadyExists_thenReturnConflict() {
        Mockito.when(userService.update(userDto.getId(), userDto)).thenThrow(EmailAlreadyUsedException.class);

        mockMvc.perform(patch(BASE_ENDPOINT + "/{id}", userDto.getId())
                        .content(objectMapper.writeValueAsBytes(userDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @SneakyThrows
    @Test
    public void getById_whenUserExists_thenReturnUser() {
        final long userId = userDto.getId();
        Mockito.when(userService.getById(userId)).thenReturn(userDto);

        mockMvc.perform(get(BASE_ENDPOINT + "/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        verify(userService).getById(userId);
    }

    @SneakyThrows
    @Test
    public void getById_whenUserNotExists_thenReturnNotFound() {
        final long userNotExistedId = 9999L;
        Mockito.when(userService.getById(anyLong())).thenThrow(UserNotFoundException.class);

        mockMvc.perform(get(BASE_ENDPOINT + "/{id}", userNotExistedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    public void getAll() {
        mockMvc.perform(get(BASE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService).getAll();
    }

    @SneakyThrows
    @Test
    public void deleteById() {
        final long userID = 1L;

        mockMvc.perform(delete(BASE_ENDPOINT + "/{id}", userID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService).delete(userID);
    }
}
