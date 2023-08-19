package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.validation.BookingStateValidator;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.common.ConstantParamStorage.*;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    // Добавление нового запроса на бронирование.
    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                         @Valid @RequestBody BookingCreateDto newBooking) {
        log.info(String.format("POST /bookings, body = %s, %s = %s", newBooking, USER_ID_REQUEST_HEADER, userId));
        return bookingClient.create(newBooking, userId);
    }

    // Подтверждение или отклонение запроса на бронирование.
    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                                 @PathVariable(name = "bookingId") long bookingId,
                                                 @RequestParam(name = "approved") boolean approved) {
        log.info(String.format("PATCH /bookings/{bookingId}?approved={approved}, {bookingId} = %s, %s = %s, {approved} = %s", bookingId, USER_ID_REQUEST_HEADER, userId, approved));
        return bookingClient.approve(bookingId, userId, approved);
    }

    // Получение данных о конкретном бронировании (включая его статус).
    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                             @PathVariable(name = "bookingId") long bookingId) {
        log.info(String.format("GET /bookings/{bookingId}, {bookingId} = %s, %s = %s", bookingId, USER_ID_REQUEST_HEADER, userId));
        return bookingClient.getBooking(bookingId, userId);
    }

    // Получение списка всех бронирований текущего пользователя (т.е список всех заявок на бронирование созданных данным пользователем).
    // Бронирования должны возвращаться отсортированными по дате от более новых к более старым.
    @GetMapping
    public ResponseEntity<Object> getUserBookingsByState(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                                         @RequestParam(name = "state", required = false) String stateStr,
                                                         @PositiveOrZero @RequestParam(name = "from", defaultValue = DEFAULT_FROM_PARAM) Integer from,
                                                         @Positive @RequestParam(name = "size", defaultValue = DEFAULT_SIZE_PARAM) Integer size) {
        final String logStr = "GET /bookings?state={state}&from={from}&size={size}, {state} = %s, {from} = %s, {size} = %s, %s = %s";
        log.info(String.format(logStr, stateStr, from, size, USER_ID_REQUEST_HEADER, userId));
        final BookingState state = BookingStateValidator.validateAndGet(stateStr);
        return bookingClient.getUserBookingsByState(userId, state, from, size);
    }

    // Получение списка бронирований для всех вещей текущего пользователя. (т.е все заявки на бронирование вещей данного пользователя.)
    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByItemOwner(@RequestHeader(USER_ID_REQUEST_HEADER) long ownerId,
                                                         @RequestParam(name = "state", required = false) String stateStr,
                                                         @PositiveOrZero @RequestParam(name = "from", defaultValue = DEFAULT_FROM_PARAM) Integer from,
                                                         @Positive @RequestParam(name = "size", defaultValue = DEFAULT_SIZE_PARAM) Integer size) {
        final String logStr = "GET /bookings/owner?state={state}&from={from}&size={size}, {state} = %s, {from} = %s, {size} = %s, %s = %s";
        log.info(String.format(logStr, stateStr, from, size, USER_ID_REQUEST_HEADER, ownerId));
        final BookingState state = BookingStateValidator.validateAndGet(stateStr);
        return bookingClient.getBookingsByItemOwner(ownerId, state, from, size);
    }
}
