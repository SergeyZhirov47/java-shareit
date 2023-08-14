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
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.exception.NotOwnerAccessException;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.ConstantParamStorage.USER_ID_REQUEST_HEADER;

@WebMvcTest(controllers = ItemController.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemControllerTest {
    private static final String BASE_ENDPOINT = "/items";
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    @MockBean
    private final ItemService itemService;

    private final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("Item")
            .description("description")
            .isAvailable(true)
            .build();
    private final long userId = 1L;
    private final long itemId = 1L;

    @SneakyThrows
    @Test
    public void add_whenValidItem_thenReturnItem() {
        final ItemCreateDto correctItem = ItemCreateDto.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .isAvailable(itemDto.getIsAvailable())
                .build();

        Mockito.when(itemService.createAndGet(correctItem, userId)).thenReturn(itemDto);

        mockMvc.perform(post(BASE_ENDPOINT)
                        .content(objectMapper.writeValueAsBytes(correctItem))
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getIsAvailable()));

        verify(itemService).createAndGet(correctItem, userId);
    }

    @SneakyThrows
    @Test
    public void add_whenMissingUserIdHeader_thenReturn500() {
        final ItemCreateDto correctItem = ItemCreateDto.builder()
                .name("Item")
                .description("description")
                .isAvailable(true)
                .build();

        Mockito.when(itemService.createAndGet(correctItem, userId)).thenReturn(itemDto);

        mockMvc.perform(post(BASE_ENDPOINT)
                        .content(objectMapper.writeValueAsBytes(correctItem))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(itemService, never()).createAndGet(correctItem, userId);
    }

    @SneakyThrows
    @Test
    public void add_whenUserNotExists_thenReturn404() {
        final ItemCreateDto correctItem = ItemCreateDto.builder()
                .name("Item")
                .description("description")
                .isAvailable(true)
                .build();
        final long userId = 9999L;

        Mockito.when(itemService.createAndGet(correctItem, userId)).thenThrow(UserNotFoundException.class);

        mockMvc.perform(post(BASE_ENDPOINT)
                        .content(objectMapper.writeValueAsBytes(correctItem))
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

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

        verify(itemService, never()).createAndGet(correctItem, userId);
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

        verify(itemService, never()).createAndGet(correctItem, userId);
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

        verify(itemService, never()).createAndGet(correctItem, userId);
    }

    @SneakyThrows
    @Test
    public void update_whenItemCorrect_thenReturnUpdatedItem() {
        final ItemDto updatedItem = ItemDto.builder()
                .id(itemId)
                .name("updated Item name")
                .description("updated Item description")
                .isAvailable(false)
                .build();

        Mockito.when(itemService.update(itemId, updatedItem, userId)).thenReturn(updatedItem);

        mockMvc.perform(patch(BASE_ENDPOINT + "/{itemId}", itemId)
                        .content(objectMapper.writeValueAsBytes(updatedItem))
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedItem.getId()))
                .andExpect(jsonPath("$.name").value(updatedItem.getName()))
                .andExpect(jsonPath("$.description").value(updatedItem.getDescription()))
                .andExpect(jsonPath("$.available").value(updatedItem.getIsAvailable()));

        verify(itemService).update(itemId, updatedItem, userId);
    }

    @SneakyThrows
    @Test
    public void update_whenMissingUserIdHeader_thenReturn500() {
        final ItemDto updatedItem = ItemDto.builder()
                .id(itemId)
                .name("updated Item name")
                .description("updated Item description")
                .isAvailable(false)
                .build();

        mockMvc.perform(patch(BASE_ENDPOINT + "/{itemId}", itemId)
                        .content(objectMapper.writeValueAsBytes(updatedItem))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(itemService, never()).update(itemId, updatedItem, userId);
    }

    @SneakyThrows
    @Test
    public void update_whenUpdateNotOwner_thenReturn403() {
        final ItemDto updatedItem = ItemDto.builder()
                .id(itemId)
                .name("updated Item name")
                .description("updated Item description")
                .isAvailable(false)
                .build();

        Mockito.when(itemService.update(itemId, updatedItem, userId)).thenThrow(NotOwnerAccessException.class);

        mockMvc.perform(patch(BASE_ENDPOINT + "/{itemId}", itemId)
                        .content(objectMapper.writeValueAsBytes(updatedItem))
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @SneakyThrows
    @Test
    public void update_whenUpdateOnlyAvailable_thenReturnUpdatedItem() {
        final ItemDto updatedItem = ItemDto.builder()
                .isAvailable(false)
                .build();

        Mockito.when(itemService.update(itemId, updatedItem, userId)).thenReturn(updatedItem);

        mockMvc.perform(patch(BASE_ENDPOINT + "/{itemId}", itemId)
                        .content(objectMapper.writeValueAsBytes(updatedItem))
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void update_whenUpdateOnlyDescription_thenReturnUpdatedItem() {
        final ItemDto updatedItem = ItemDto.builder()
                .description("new description")
                .build();

        Mockito.when(itemService.update(itemId, updatedItem, userId)).thenReturn(updatedItem);

        mockMvc.perform(patch(BASE_ENDPOINT + "/{itemId}", itemId)
                        .content(objectMapper.writeValueAsBytes(updatedItem))
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void update_whenUpdateOnlyName_thenReturnUpdatedItem() {
        final ItemDto updatedItem = ItemDto.builder()
                .name("new name")
                .build();

        Mockito.when(itemService.update(itemId, updatedItem, userId)).thenReturn(updatedItem);

        mockMvc.perform(patch(BASE_ENDPOINT + "/{itemId}", itemId)
                        .content(objectMapper.writeValueAsBytes(updatedItem))
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void getItemById_whenOk_thenReturnItem() {
        final ItemWithAdditionalDataDto itemWithAdditionalDataDto = ItemWithAdditionalDataDto.builder()
                .id(itemId)
                .name("Item")
                .description("description")
                .isAvailable(true)
                .build();

        Mockito.when(itemService.getById(itemId, userId)).thenReturn(itemWithAdditionalDataDto);

        mockMvc.perform(get(BASE_ENDPOINT + "/{itemId}", itemId)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemService).getById(itemId, userId);
    }

    @SneakyThrows
    @Test
    public void getItemById_whenUserNotExists_thenReturn404() {
        Mockito.when(itemService.getById(itemId, userId)).thenThrow(UserNotFoundException.class);

        mockMvc.perform(get(BASE_ENDPOINT + "/{itemId}", itemId)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    public void searchItems_whenHasSearchText_thenReturnOk() {
        final String searchText = "want best item in the world";
        Mockito.when(itemService.searchItems(searchText, userId)).thenReturn(List.of(itemDto));

        mockMvc.perform(get(BASE_ENDPOINT + "/search")
                        .param("text", searchText)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void searchItems_whenEmptySearchText_thenReturnOk() {
        final String emptySearchString = "";
        Mockito.when(itemService.searchItems(emptySearchString, userId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT + "/search")
                        .param("text", emptySearchString)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void searchItems_whenNoSearchText_thenReturn500() {
        mockMvc.perform(get(BASE_ENDPOINT + "/search")
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @SneakyThrows
    @Test
    public void getAllOwnerItems_whenOk_thenReturnItems() {
        Mockito.when(itemService.getAllOwnerItems(userId, null, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemService).getAllOwnerItems(userId, null, null);
    }

    @SneakyThrows
    @Test
    public void getAllOwnerItems_whenOkWithPagination_thenReturnItems() {
        final Integer from = 0;
        final Integer size = 5;
        Mockito.when(itemService.getAllOwnerItems(userId, from, size)).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemService).getAllOwnerItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    public void getAllOwnerItems_whenUserNotExists_thenReturn404() {
        Mockito.when(itemService.getAllOwnerItems(userId, null, null)).thenThrow(UserNotFoundException.class);

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    public void getAllOwnerItems_whenHasFromAndNoSize_thenReturn400() {
        final Integer from = 0;
        Mockito.when(itemService.getAllOwnerItems(userId, from, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    public void getAllOwnerItems_whenNoFromAndHasSize_thenReturn400() {
        final Integer size = 10;
        Mockito.when(itemService.getAllOwnerItems(userId, null, size)).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    public void getAllOwnerItems_whenFromAndHasIsZero_thenReturn500() {
        final Integer from = 0;
        final Integer size = 0;
        Mockito.when(itemService.getAllOwnerItems(userId, from, size)).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(itemService, never()).getAllOwnerItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    public void getAllOwnerItems_whenFromNegative_thenReturn500() {
        final Integer from = -1;
        final Integer size = 10;
        Mockito.when(itemService.getAllOwnerItems(userId, from, size)).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(itemService, never()).getAllOwnerItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    public void getAllOwnerItems_whenSizeNegative_thenReturn500() {
        final Integer from = 0;
        final Integer size = -10;
        Mockito.when(itemService.getAllOwnerItems(userId, from, size)).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(itemService, never()).getAllOwnerItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    public void addComment_whenValidComment_thenReturnComment() {
        final CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .text("Comment text")
                .build();

        Mockito.when(itemService.addComment(itemId, userId, commentCreateDto)).thenReturn(CommentDto.builder().build());

        mockMvc.perform(post(BASE_ENDPOINT + "/{itemId}/comment", itemId)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .content(objectMapper.writeValueAsBytes(commentCreateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemService).addComment(itemId, userId, commentCreateDto);
    }

    @SneakyThrows
    @Test
    public void addComment_whenEmptyComment_thenReturn400() {
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

        verify(itemService, never()).addComment(itemId, userId, emptyComment);
    }

    @SneakyThrows
    @Test
    public void addComment_whenNoBooking_thenReturn400() {
        final CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .text("Comment text")
                .build();

        Mockito.when(itemService.addComment(itemId, userId, commentCreateDto)).thenThrow(UnsupportedOperationException.class);

        mockMvc.perform(post(BASE_ENDPOINT + "/{itemId}/comment", itemId)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .content(objectMapper.writeValueAsBytes(commentCreateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
