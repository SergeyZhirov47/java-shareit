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
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserControllerTest {
    private final static String BASE_ENDPOINT = "/users";
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    @MockBean
    private final UserService userService;
    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("John Doe")
            .email("johnDoe@somemail.com")
            .build();

    @Test
    public void create() throws Exception {
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
                .andExpect(jsonPath("$.id", is(userDto.getId())))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService).createAndGet(correctUser);
    }

    @Test
    public void create_thenNoEmail_thenReturnBadRequest() throws Exception {
        final UserCreateDto userWithoutEmail = UserCreateDto.builder()
                .name("John Doe")
                .build();

        mockMvc.perform(post(BASE_ENDPOINT)
                        .content(objectMapper.writeValueAsBytes(userWithoutEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_thenInvalidEmail_thenReturnBadRequest() throws Exception {
        final UserCreateDto userWithInvalidEmail = UserCreateDto.builder()
                .name("John Doe")
                .email("not valid email")
                .build();

        mockMvc.perform(post(BASE_ENDPOINT)
                        .content(objectMapper.writeValueAsBytes(userWithInvalidEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void update() {

    }

    @SneakyThrows
    @Test
    public void getById() {
        var userId = 1L;
        final UserDto userDto = UserDto.builder()
                .id(userId)
                .build();

        Mockito.when(userService.getById(userId)).thenReturn(userDto);

        mockMvc.perform(get(BASE_ENDPOINT + "/{id}", userId))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).getById(userId);
    }

    @Test
    public void getAll() {

    }

    @Test
    public void deleteById() {

    }
}
