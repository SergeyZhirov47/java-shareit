package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.ConstantParamStorage.USER_ID_REQUEST_HEADER;

@WebMvcTest(controllers = ItemController.class)
@ContextConfiguration(classes = ShareItGateway.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemControllerTest {
    private static final String BASE_ENDPOINT = "/items";
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    @MockBean
    private final ItemClient itemClient;
    private final long userId = 1L;

    @SneakyThrows
    @Test
    public void add_whenItemWithoutAvailable_thenReturn400() {
        final ItemCreateDto correctItem = ItemCreateDto.builder()
                .name("Item")
                .description("description")
                .build();

        mockMvc.perform(post(BASE_ENDPOINT)
                        .content(objectMapper.writeValueAsBytes(correctItem))
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createAndGet(correctItem, userId);
    }

    @SneakyThrows
    @Test
    public void add_whenItemHasEmptyName_thenReturn400() {
        final ItemCreateDto correctItem = ItemCreateDto.builder()
                .name("")
                .description("description")
                .isAvailable(true)
                .build();

        mockMvc.perform(post(BASE_ENDPOINT)
                        .content(objectMapper.writeValueAsBytes(correctItem))
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createAndGet(correctItem, userId);
    }

    @SneakyThrows
    @Test
    public void add_whenItemWithoutDescription_thenReturn400() {
        final ItemCreateDto correctItem = ItemCreateDto.builder()
                .name("Item")
                .isAvailable(true)
                .build();

        mockMvc.perform(post(BASE_ENDPOINT)
                        .content(objectMapper.writeValueAsBytes(correctItem))
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createAndGet(correctItem, userId);
    }

    @SneakyThrows
    @Test
    public void getAllOwnerItems_whenFromAndHasIsZero_thenReturn500() {
        final Integer from = 0;
        final Integer size = 0;
        Mockito.when(itemClient.getAllOwnerItems(userId, from, size))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(itemClient, never()).getAllOwnerItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    public void getAllOwnerItems_whenFromNegative_thenReturn500() {
        final Integer from = -1;
        final Integer size = 10;
        Mockito.when(itemClient.getAllOwnerItems(userId, from, size))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(itemClient, never()).getAllOwnerItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    public void getAllOwnerItems_whenSizeNegative_thenReturn500() {
        final Integer from = 0;
        final Integer size = -10;
        Mockito.when(itemClient.getAllOwnerItems(userId, from, size))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(itemClient, never()).getAllOwnerItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    public void addComment_whenEmptyComment_thenReturn400() {
        final long itemId = 1L;
        final CommentCreateDto emptyComment = CommentCreateDto.builder()
                .text("")
                .build();

        mockMvc.perform(post(BASE_ENDPOINT + "/{itemId}/comment", itemId)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .content(objectMapper.writeValueAsBytes(emptyComment))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).addComment(itemId, userId, emptyComment);
    }
}
