package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStateForSearch;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.validation.BookingStateForSearchHelper;

import java.util.List;

import static ru.practicum.shareit.common.ConstantParamStorage.USER_ID_REQUEST_HEADER;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    // Добавление нового запроса на бронирование.
    @PostMapping
    public BookingDto create(@RequestHeader(USER_ID_REQUEST_HEADER) long userId, @RequestBody BookingCreateDto newBooking) {
        log.info(String.format("POST /bookings, body = %s, %s = %s", newBooking, USER_ID_REQUEST_HEADER, userId));
        final BookingDto bookingDto = bookingService.create(newBooking, userId);
        log.info(String.format("Успешно создана заявка на бронирование предмета с id = %s от пользователя с id = %s", newBooking.getItemId(), userId));

        return bookingDto;
    }

    // Подтверждение или отклонение запроса на бронирование.
    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                     @PathVariable(name = "bookingId") long bookingId,
                                     @RequestParam(name = "approved") boolean approved) {
        log.info(String.format("PATCH /bookings/{bookingId}?approved={approved}, {bookingId} = %s, %s = %s, {approved} = %s", bookingId, USER_ID_REQUEST_HEADER, userId, approved));
        final BookingDto bookingDto = bookingService.approve(bookingId, userId, approved);
        log.info(String.format("Владелец вещи изменил статус запроса бронирования на %s", bookingDto.getStatus()));

        return bookingDto;
    }

    // Получение данных о конкретном бронировании (включая его статус).
    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader(USER_ID_REQUEST_HEADER) long userId, @PathVariable(name = "bookingId") long bookingId) {
        log.info(String.format("GET /bookings/{bookingId}, {bookingId} = %s, %s = %s", bookingId, USER_ID_REQUEST_HEADER, userId));
        final BookingDto bookingDto = bookingService.getBooking(bookingId, userId);
        log.info("Данные о бронировании успешно получены");

        return bookingDto;
    }

    // Получение списка всех бронирований текущего пользователя (т.е список всех заявок на бронирование созданных данным пользователем).
    // Бронирования должны возвращаться отсортированными по дате от более новых к более старым.
    @GetMapping
    public List<BookingDto> getUserBookingsByState(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                                   @RequestParam(name = "state", required = false) String stateStr,
                                                   @RequestParam(name = "from", required = false) Integer from,
                                                   @RequestParam(name = "size", required = false) Integer size) {
        final String logStr = "GET /bookings?state={state}&from={from}&size={size}, {state} = %s, {from} = %s, {size} = %s, %s = %s";
        log.info(String.format(logStr, stateStr, from, size, USER_ID_REQUEST_HEADER, userId));
        final BookingStateForSearch state = BookingStateForSearchHelper.convertFromString(stateStr);
        final List<BookingDto> userBookings = bookingService.getUserBookingsByState(userId, state, from, size);
        log.info(String.format("Список всех заявок на бронирование, созданных пользователем id = %s успешно получен", userId));

        return userBookings;
    }

    // Получение списка бронирований для всех вещей текущего пользователя. (т.е все заявки на бронирование вещей данного пользователя.)
    @GetMapping("/owner")
    public List<BookingDto> getBookingsByItemOwner(@RequestHeader(USER_ID_REQUEST_HEADER) long ownerId,
                                                   @RequestParam(name = "state", required = false) String stateStr,
                                                   @RequestParam(name = "from", required = false) Integer from,
                                                   @RequestParam(name = "size", required = false) Integer size) {
        final String logStr = "GET /bookings/owner?state={state}&from={from}&size={size}, {state} = %s, {from} = %s, {size} = %s, %s = %s";
        log.info(String.format(logStr, stateStr, from, size, USER_ID_REQUEST_HEADER, ownerId));
        final BookingStateForSearch state = BookingStateForSearchHelper.convertFromString(stateStr);
        final List<BookingDto> ownerBookings = bookingService.getBookingsByItemOwner(ownerId, state, from, size);
        log.info(String.format("Список всех заявок на бронирование вещей пользователя id = %s успешно получен", ownerId));

        return ownerBookings;
    }
}
