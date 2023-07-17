package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStateForSearch;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@Validated
@AllArgsConstructor
@Slf4j
public class BookingController {
    // ToDo!
    // Такой же USER_ID_REQUEST_HEADER  используется и в ItemController
    // вынести в отдельное место
    private static final String USER_ID_REQUEST_HEADER = "X-Sharer-User-Id";
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

    // ToDo
    // тут state (статус) интересный.
    // Некоторые состояния от BookingStatus, а другие свои.

    //    Получение списка всех бронирований текущего пользователя (т.е список всех заявок на бронирование созданных данным пользователем).
    //    Эндпоинт — GET /bookings?state={state}. Параметр state необязательный и по умолчанию равен ALL (англ. «все»).
    //    Также он может принимать значения CURRENT (англ. «текущие»), **PAST** (англ. «завершённые»), FUTURE (англ. «будущие»),
    //    WAITING (англ. «ожидающие подтверждения»), REJECTED (англ. «отклонённые»).
    //    Бронирования должны возвращаться отсортированными по дате от более новых к более старым.
    @GetMapping
    public List<BookingDto> getUserBookingsByState(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                              @RequestParam(name = "state", defaultValue = "ALL") BookingStateForSearch state) {
        log.info(String.format("GET /bookings?state={state}, {state} = %s, %s = %s", state, USER_ID_REQUEST_HEADER, userId));
        return null;
    }

    //    Получение списка бронирований для всех вещей текущего пользователя. (т.е все заявки на бронирование вещей данного пользователя.)
    //    Эндпоинт — GET /bookings/owner?state={state}. Этот запрос имеет смысл для владельца хотя бы одной вещи.
    //    Работа параметра state аналогична его работе в предыдущем сценарии.
    @GetMapping("/owner")
    public List<BookingDto> getBookingItemsByOwner(@RequestHeader(USER_ID_REQUEST_HEADER) long ownerId,
                                                   @RequestParam(name = "state", defaultValue = "All") BookingStateForSearch state) {
        log.info(String.format("GET /bookings/owner?state={state}, {state} = %s, %s = %s", state, USER_ID_REQUEST_HEADER, ownerId));

        return null;
    }
}
