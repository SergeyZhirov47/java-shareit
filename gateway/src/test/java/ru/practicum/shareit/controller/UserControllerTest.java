package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserCreateDto;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = ShareItGateway.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserControllerTest {
    private static final String BASE_ENDPOINT = "/users";
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    @MockBean
    private final UserClient userClient;

    @SneakyThrows
    @Test
    public void create_thenInvalidEmail_thenReturnBadRequest() {
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

        verify(userClient, never()).createAndGet(userWithInvalidEmail);
    }

    @SneakyThrows
    @Test
    public void create_thenNoEmail_thenReturnBadRequest() {
        final UserCreateDto userWithoutEmail = UserCreateDto.builder()
                .name("John Doe")
                .build();

        mockMvc.perform(post(BASE_ENDPOINT)
                        .content(objectMapper.writeValueAsBytes(userWithoutEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).createAndGet(userWithoutEmail);
    }
}
