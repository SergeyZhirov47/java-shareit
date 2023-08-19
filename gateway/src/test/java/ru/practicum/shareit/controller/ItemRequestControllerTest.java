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
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.ConstantParamStorage.USER_ID_REQUEST_HEADER;

@WebMvcTest(controllers = ItemRequestController.class)
@ContextConfiguration(classes = ShareItGateway.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemRequestControllerTest {
    private static final String BASE_ENDPOINT = "/requests";
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    @MockBean
    private final ItemRequestClient itemRequestClient;

    private final long userId = 1;

    @SneakyThrows
    @Test
    public void addItemRequest_whenDescriptionIsBlank_thenReturn400() {
        final ItemRequestCreateDto requestWithNullDescription = ItemRequestCreateDto.builder()
                .description("")
                .build();

        mockMvc.perform(post(BASE_ENDPOINT)
                        .content(objectMapper.writeValueAsBytes(requestWithNullDescription))
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).createAndGet(requestWithNullDescription, userId);
    }

    @SneakyThrows
    @Test
    public void addItemRequest_whenDescriptionIsNull_thenReturn400() {
        final ItemRequestCreateDto requestWithNullDescription = ItemRequestCreateDto.builder()
                .description(null)
                .build();

        mockMvc.perform(post(BASE_ENDPOINT)
                        .content(objectMapper.writeValueAsBytes(requestWithNullDescription))
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).createAndGet(requestWithNullDescription, userId);
    }
}
