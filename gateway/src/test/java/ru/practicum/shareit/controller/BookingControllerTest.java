package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.validation.ValidationException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.ConstantParamStorage.USER_ID_REQUEST_HEADER;

@WebMvcTest(controllers = BookingController.class)
@ContextConfiguration(classes = ShareItGateway.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingControllerTest {
    private static final String BASE_ENDPOINT = "/bookings";
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    @MockBean
    private final BookingClient bookingClient;

    private final long userId = 1;
    private final long itemId = 1;

    @SneakyThrows
    @Test
    public void getUserBookingsByState_whenUnsupportedState_thenReturn400() {
        final String state = "UNSUPPORTED STATE";

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("state", state)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getUserBookingsByState(anyLong(), any(BookingState.class), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    public void getBookingsByItemOwner_whenUnsupportedState_thenReturn400() {
        final String stateStr = "UNSUPPORTED STATE";

        Mockito.when(bookingClient.getBookingsByItemOwner(userId, BookingState.ALL, null, null))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mockMvc.perform(get(BASE_ENDPOINT + "/owner")
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("state", stateStr)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getUserBookingsByState(anyLong(), any(BookingState.class), anyInt(), anyInt());
    }

    @Nested
    public class TestCreateBooking {
        private LocalDateTime start;
        private LocalDateTime end;
        private BookingCreateDto bookingCreateDto;

        @BeforeEach
        public void init() {
            start = LocalDateTime.now().withNano(0).plusMinutes(1);
            end = start.plusDays(1);

            bookingCreateDto = BookingCreateDto.builder()
                    .itemId(itemId)
                    .start(start)
                    .end(end)
                    .build();
        }

        @SneakyThrows
        @Test
        public void create_whenValidBooking_thenReturnOk() {
            Mockito.when(bookingClient.create(bookingCreateDto, userId)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

            mockMvc.perform(post(BASE_ENDPOINT)
                            .content(objectMapper.writeValueAsBytes(bookingCreateDto))
                            .header(USER_ID_REQUEST_HEADER, userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(bookingClient).create(bookingCreateDto, userId);
        }

        @SneakyThrows
        @Test
        public void create_whenItemNotAvailable_thenReturn400() {
            Mockito.when(bookingClient.create(bookingCreateDto, userId)).thenThrow(UnsupportedOperationException.class);

            mockMvc.perform(post(BASE_ENDPOINT)
                            .content(objectMapper.writeValueAsBytes(bookingCreateDto))
                            .header(USER_ID_REQUEST_HEADER, userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @SneakyThrows
        @Test
        public void create_whenStartEndNotValid_thenReturn400() {
            Mockito.when(bookingClient.create(bookingCreateDto, userId)).thenThrow(ValidationException.class);

            mockMvc.perform(post(BASE_ENDPOINT)
                            .content(objectMapper.writeValueAsBytes(bookingCreateDto))
                            .header(USER_ID_REQUEST_HEADER, userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @SneakyThrows
        @Test
        public void create_whenStartInPast_thenReturn400() {
            start = LocalDateTime.now().withNano(0).minusDays(1);
            bookingCreateDto = BookingCreateDto.builder()
                    .itemId(itemId)
                    .start(start)
                    .end(end)
                    .build();

            mockMvc.perform(post(BASE_ENDPOINT)
                            .content(objectMapper.writeValueAsBytes(bookingCreateDto))
                            .header(USER_ID_REQUEST_HEADER, userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(bookingClient, never()).create(bookingCreateDto, userId);
        }

        @SneakyThrows
        @Test
        public void create_whenEndInPast_thenReturn400() {
            end = LocalDateTime.now().withNano(0).minusDays(1);
            bookingCreateDto = BookingCreateDto.builder()
                    .itemId(itemId)
                    .start(start)
                    .end(end)
                    .build();

            mockMvc.perform(post(BASE_ENDPOINT)
                            .content(objectMapper.writeValueAsBytes(bookingCreateDto))
                            .header(USER_ID_REQUEST_HEADER, userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(bookingClient, never()).create(bookingCreateDto, userId);
        }

        @SneakyThrows
        @Test
        public void create_whenStartIsNull_thenReturn400() {
            start = null;
            bookingCreateDto = BookingCreateDto.builder()
                    .itemId(itemId)
                    .start(start)
                    .end(end)
                    .build();

            mockMvc.perform(post(BASE_ENDPOINT)
                            .content(objectMapper.writeValueAsBytes(bookingCreateDto))
                            .header(USER_ID_REQUEST_HEADER, userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(bookingClient, never()).create(bookingCreateDto, userId);
        }

        @SneakyThrows
        @Test
        public void create_whenEndIsNull_thenReturn400() {
            end = null;
            bookingCreateDto = BookingCreateDto.builder()
                    .itemId(itemId)
                    .start(start)
                    .end(end)
                    .build();

            mockMvc.perform(post(BASE_ENDPOINT)
                            .content(objectMapper.writeValueAsBytes(bookingCreateDto))
                            .header(USER_ID_REQUEST_HEADER, userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(bookingClient, never()).create(bookingCreateDto, userId);
        }
    }
}
