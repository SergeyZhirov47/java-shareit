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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.model.BookingStateForSearch;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.NotFoundException;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.ConstantParamStorage.USER_ID_REQUEST_HEADER;

@WebMvcTest(controllers = BookingController.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingControllerTest {
    private static final String BASE_ENDPOINT = "/bookings";
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    @MockBean
    private final BookingService bookingService;

    private final long itemId = 1;
    private final long userId = 1;
    private final long bookingId = 1;

    @SneakyThrows
    @Test
    public void approveBooking_whenOk_thenReturnOk() {
        final boolean approved = true;
        Mockito.when(bookingService.approve(bookingId, userId, approved)).thenReturn(BookingDto.builder().build());

        mockMvc.perform(patch(BASE_ENDPOINT + "/{bookingId}", bookingId)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService).approve(bookingId, userId, approved);
    }

    @SneakyThrows
    @Test
    public void approveBooking_whenBookingNotExists_thenReturn404() {
        final boolean approved = true;
        Mockito.when(bookingService.approve(bookingId, userId, approved)).thenThrow(BookingNotFoundException.class);

        mockMvc.perform(patch(BASE_ENDPOINT + "/{bookingId}", bookingId)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    public void approveBooking_whenBookingWrongStatus_thenReturn400() {
        final boolean approved = true;
        Mockito.when(bookingService.approve(bookingId, userId, approved)).thenThrow(UnsupportedOperationException.class);

        mockMvc.perform(patch(BASE_ENDPOINT + "/{bookingId}", bookingId)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    public void approveBooking_whenBookingNotOwnerApproves_thenReturn404() {
        final boolean approved = true;
        Mockito.when(bookingService.approve(bookingId, userId, approved)).thenThrow(NotFoundException.class);

        mockMvc.perform(patch(BASE_ENDPOINT + "/{bookingId}", bookingId)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    public void getBooking_whenBookingExists_thenReturnOk() {
        Mockito.when(bookingService.getBooking(bookingId, userId)).thenReturn(BookingDto.builder().build());

        mockMvc.perform(get(BASE_ENDPOINT + "/{bookingId}", bookingId)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService).getBooking(bookingId, userId);
    }

    @SneakyThrows
    @Test
    public void getBooking_whenBookingNotExists_thenReturn404() {
        Mockito.when(bookingService.getBooking(bookingId, userId)).thenThrow(BookingNotFoundException.class);

        mockMvc.perform(get(BASE_ENDPOINT + "/{bookingId}", bookingId)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    public void getUserBookingsByState_whenOk_thenReturnOk() {
        Mockito.when(bookingService.getUserBookingsByState(userId, BookingStateForSearch.ALL, null, null))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService).getUserBookingsByState(userId, BookingStateForSearch.ALL, null, null);
    }

    @SneakyThrows
    @Test
    public void getUserBookingsByState_whenUserNotExists_thenReturn404() {
        Mockito.when(bookingService.getUserBookingsByState(userId, BookingStateForSearch.ALL, null, null))
                .thenThrow(UserNotFoundException.class);

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookingService).getUserBookingsByState(userId, BookingStateForSearch.ALL, null, null);
    }

    @SneakyThrows
    @Test
    public void getUserBookingsByState_whenHasState_thenReturnOk() {
        final BookingStateForSearch state = BookingStateForSearch.ALL;
        final String stateStr = state.name();
        Mockito.when(bookingService.getUserBookingsByState(userId, state, null, null))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT)
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("state", stateStr)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService).getUserBookingsByState(userId, state, null, null);
    }

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

        verify(bookingService, never()).getUserBookingsByState(anyLong(), any(BookingStateForSearch.class), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    public void getBookingsByItemOwner_whenOk_thenReturnOk() {
        Mockito.when(bookingService.getBookingsByItemOwner(userId, BookingStateForSearch.ALL, null, null))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT + "/owner")
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService).getBookingsByItemOwner(userId, BookingStateForSearch.ALL, null, null);
    }

    @SneakyThrows
    @Test
    public void getBookingsByItemOwner_whenUserNotExists_thenReturn404() {
        Mockito.when(bookingService.getBookingsByItemOwner(userId, BookingStateForSearch.ALL, null, null))
                        .thenThrow(UserNotFoundException.class);

        mockMvc.perform(get(BASE_ENDPOINT + "/owner")
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookingService).getBookingsByItemOwner(userId, BookingStateForSearch.ALL, null, null);
    }

    @SneakyThrows
    @Test
    public void getBookingsByItemOwner_whenHasState_thenReturnOk() {
        final BookingStateForSearch state = BookingStateForSearch.ALL;
        final String stateStr = state.name();
        Mockito.when(bookingService.getBookingsByItemOwner(userId, state, null, null))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT + "/owner")
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("state", stateStr)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService).getBookingsByItemOwner(userId, state, null, null);
    }

    @SneakyThrows
    @Test
    public void getBookingsByItemOwner_whenUnsupportedState_thenReturn400() {
        final String stateStr = "UNSUPPORTED STATE";

        Mockito.when(bookingService.getBookingsByItemOwner(userId, BookingStateForSearch.ALL, null, null))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT + "/owner")
                        .header(USER_ID_REQUEST_HEADER, userId)
                        .param("state", stateStr)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getUserBookingsByState(anyLong(), any(BookingStateForSearch.class), anyInt(), anyInt());
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
            Mockito.when(bookingService.create(bookingCreateDto, userId)).thenReturn(BookingDto.builder().build());

            mockMvc.perform(post(BASE_ENDPOINT)
                            .content(objectMapper.writeValueAsBytes(bookingCreateDto))
                            .header(USER_ID_REQUEST_HEADER, userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(bookingService).create(bookingCreateDto, userId);
        }

        @SneakyThrows
        @Test
        public void create_whenItemNotAvailable_thenReturn400() {
            Mockito.when(bookingService.create(bookingCreateDto, userId)).thenThrow(UnsupportedOperationException.class);

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
        public void create_whenUserNotExists_thenReturn404() {
            Mockito.when(bookingService.create(bookingCreateDto, userId)).thenThrow(UserNotFoundException.class);

            mockMvc.perform(post(BASE_ENDPOINT)
                            .content(objectMapper.writeValueAsBytes(bookingCreateDto))
                            .header(USER_ID_REQUEST_HEADER, userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @SneakyThrows
        @Test
        public void create_whenItemNotExists_thenReturn404() {
            Mockito.when(bookingService.create(bookingCreateDto, userId)).thenThrow(ItemNotFoundException.class);

            mockMvc.perform(post(BASE_ENDPOINT)
                            .content(objectMapper.writeValueAsBytes(bookingCreateDto))
                            .header(USER_ID_REQUEST_HEADER, userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @SneakyThrows
        @Test
        public void create_whenStartEndNotValid_thenReturn400() {
            Mockito.when(bookingService.create(bookingCreateDto, userId)).thenThrow(ValidationException.class);

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

            verify(bookingService, never()).create(bookingCreateDto, userId);
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

            verify(bookingService, never()).create(bookingCreateDto, userId);
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

            verify(bookingService, never()).create(bookingCreateDto, userId);
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

            verify(bookingService, never()).create(bookingCreateDto, userId);
        }
    }
}
