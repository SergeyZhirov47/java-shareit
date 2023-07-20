package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStateForSearch;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.ValidationException;

import javax.validation.Valid;
import java.util.List;

import static java.util.Objects.isNull;
import static ru.practicum.shareit.common.RequestHeaderName.USER_ID_REQUEST_HEADER;

@RestController
@RequestMapping(path = "/bookings")
@Validated
@AllArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    // Добавление нового запроса на бронирование.
    @PostMapping
    public BookingDto create(@RequestHeader(USER_ID_REQUEST_HEADER) long userId, @Valid @RequestBody BookingCreateDto newBooking) {
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
                                                   @RequestParam(name = "state", required = false) String stateStr) {
        log.info(String.format("GET /bookings?state={state}, {state} = %s, %s = %s", stateStr, USER_ID_REQUEST_HEADER, userId));

        // ToDo
        // этот код валидации нужно куда-то пристроить...
        // - в само enum... Дефолтное значение наверное разве, что передавать (а то с фига он знает, что по дефолту). Хотя ALL. Даже не знаю
        // - в сервис? с одной стороны логично. Там и будет дефолтное значение задаваться. бизнес логика как никак. Но из сервиса торчит метод валидации. Фу
        // - в сервисе просто добавить еще один метод getUserBookingsByState, который принимает String? не очень.
        BookingStateForSearch state;
        if (isNull(stateStr)) {
            state = BookingStateForSearch.ALL;
        } else {
            try {
                state = BookingStateForSearch.valueOf(stateStr);
            } catch (IllegalArgumentException exp) {
                throw new ValidationException(String.format("Unknown state: %s", stateStr));
            }
        }

        final List<BookingDto> userBookings = bookingService.getUserBookingsByState(userId, state);
        log.info(String.format("Список всех заявок на бронирование, созданных пользователем id = %s успешно получен", userId));

        return userBookings;
    }

    // Получение списка бронирований для всех вещей текущего пользователя. (т.е все заявки на бронирование вещей данного пользователя.)
    @GetMapping("/owner")
    public List<BookingDto> getBookingsByItemOwner(@RequestHeader(USER_ID_REQUEST_HEADER) long ownerId,
                                                   @RequestParam(name = "state", required = false) String stateStr) {
        log.info(String.format("GET /bookings/owner?state={state}, {state} = %s, %s = %s", stateStr, USER_ID_REQUEST_HEADER, ownerId));

        // ToDo
        // этот код валидации нужно куда-то пристроить...
        // - в само enum... Дефолтное значение наверное разве, что передавать (а то с фига он знает, что по дефолту). Хотя ALL. Даже не знаю
        // - в сервис? с одной стороны логично. Там и будет дефолтное значение задаваться. бизнес логика как никак. Но из сервиса торчит метод валидации. Фу
        // - в сервисе просто добавить еще один метод getUserBookingsByState, который принимает String? не очень.
        BookingStateForSearch state;
        if (isNull(stateStr)) {
            state = BookingStateForSearch.ALL;
        } else {
            try {
                state = BookingStateForSearch.valueOf(stateStr);
            } catch (IllegalArgumentException exp) {
                throw new ValidationException(String.format("Unknown state: %s", stateStr));
            }
        }

        final List<BookingDto> ownerBookings = bookingService.getBookingsByItemOwner(ownerId, state);
        log.info(String.format("Список всех заявок на бронирование вещей пользователя id = %s успешно получен", ownerId));

        return ownerBookings;
    }
}
