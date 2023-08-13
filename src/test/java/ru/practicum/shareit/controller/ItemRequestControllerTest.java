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
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.repository.DaoUser;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.ConstantParamStorage.USER_ID_REQUEST_HEADER;

@WebMvcTest(controllers = ItemRequestController.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemRequestControllerTest {
    private static final String BASE_ENDPOINT = "/requests";
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    @MockBean
    private final ItemRequestService itemRequestService;

    private final long userId = 1;
    private final long requestId = 1;
    private final ItemRequestDto defaultEmptyItemRequestDto = ItemRequestDto.builder().build();

    @SneakyThrows
    @Test
    public void addItemRequest_whenOk_thenReturnOk() {
        final ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder()
                .description("need mock")
                .build();

        Mockito.when(itemRequestService.createAndGet(itemRequestCreateDto, userId)).thenReturn(defaultEmptyItemRequestDto);

        mockMvc.perform(post(BASE_ENDPOINT)
                        .content(objectMapper.writeValueAsBytes(itemRequestCreateDto))
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemRequestService).createAndGet(itemRequestCreateDto, userId);
    }

    @SneakyThrows
    @Test
    public void addItemRequest_whenUserNotExists_thenReturn404() {
        final ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder()
                .description("need mock")
                .build();

        Mockito.when(itemRequestService.createAndGet(itemRequestCreateDto, userId))
                .thenThrow(UserNotFoundException.class);

        mockMvc.perform(post(BASE_ENDPOINT)
                        .content(objectMapper.writeValueAsBytes(itemRequestCreateDto))
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(itemRequestService).createAndGet(itemRequestCreateDto, userId);
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

        verify(itemRequestService, never()).createAndGet(requestWithNullDescription, userId);
    }

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

        verify(itemRequestService, never()).createAndGet(requestWithNullDescription, userId);
    }

    @SneakyThrows
    @Test
    public void getAllUserItemRequests_whenOk_thenReturnOk() {
        Mockito.when(itemRequestService.getAllUserItemRequests(userId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemRequestService).getAllUserItemRequests(userId);
    }

    @SneakyThrows
    @Test
    public void getAllUserItemRequests_whenUserNotExists_thenReturn404() {
        Mockito.when(itemRequestService.getAllUserItemRequests(userId))
                .thenThrow(UserNotFoundException.class);

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(itemRequestService).getAllUserItemRequests(userId);
    }

    @SneakyThrows
    @Test
    public void getAllItemRequests_whenOk_thenReturnOk() {
        Mockito.when(itemRequestService.getAllItemRequests(userId, null, null))
                        .thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT + "/all")
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemRequestService).getAllItemRequests(userId, null, null);
    }

    @SneakyThrows
    @Test
    public void getAllItemRequests_whenUserNotExists_thenReturn404() {
        Mockito.when(itemRequestService.getAllItemRequests(userId, null, null))
                .thenThrow(UserNotFoundException.class);

        mockMvc.perform(get(BASE_ENDPOINT + "/all")
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(itemRequestService).getAllItemRequests(userId, null, null);
    }

    @SneakyThrows
    @Test
    public void getAllItemRequests_whenOkWithPagination_thenReturnOk() {
        final Integer from = 0;
        final Integer size = 10;
        Mockito.when(itemRequestService.getAllItemRequests(userId, from, size))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT + "/all")
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemRequestService).getAllItemRequests(userId, from, size);
    }

    @SneakyThrows
    @Test
    public void getItemRequest_whenOk_thenReturnOk() {
        Mockito.when(itemRequestService.getItemRequestById(requestId, userId))
                .thenReturn(defaultEmptyItemRequestDto);

        mockMvc.perform(get(BASE_ENDPOINT + "/{requestId}", requestId)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemRequestService).getItemRequestById(requestId, userId);
    }

    @SneakyThrows
    @Test
    public void getItemRequest_whenRequestNotExists_thenReturn404() {
        Mockito.when(itemRequestService.getItemRequestById(requestId, userId))
                        .thenThrow(ItemRequestNotFoundException.class);

        mockMvc.perform(get(BASE_ENDPOINT + "/{requestId}", requestId)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(itemRequestService).getItemRequestById(requestId, userId);
    }

    @SneakyThrows
    @Test
    public void getItemRequest_whenUserNotExists_thenReturn404() {
        Mockito.when(itemRequestService.getItemRequestById(requestId, userId))
                .thenThrow(UserNotFoundException.class);

        mockMvc.perform(get(BASE_ENDPOINT + "/{requestId}", requestId)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(itemRequestService).getItemRequestById(requestId, userId);
    }
}
