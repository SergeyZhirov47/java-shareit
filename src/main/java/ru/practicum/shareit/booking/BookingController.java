package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStateForSearch;
import ru.practicum.shareit.booking.service.BookingService;

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

    //    Добавление нового запроса на бронирование.
    //    Запрос может быть создан любым пользователем, а затем подтверждён владельцем вещи.
    //    Эндпоинт — POST /bookings. После создания запрос находится в статусе WAITING — «ожидает подтверждения».
    // @Valid
    @PostMapping
    public BookingDto create(@RequestHeader(USER_ID_REQUEST_HEADER) long userId, @RequestBody BookingCreateDto newBooking) {
        log.info(String.format("POST /bookings, body = %s, %s = %s", newBooking, USER_ID_REQUEST_HEADER, userId));
        final BookingDto bookingDto = bookingService.create(newBooking, userId);
        log.info(String.format("Успешно создана заявка на бронирование предмета с id = %s от пользователя с id = %s", newBooking.getItemId(), userId));

        return bookingDto;
    }

    //    Подтверждение или отклонение запроса на бронирование. Может быть выполнено только владельцем вещи.
    //    Затем статус бронирования становится либо APPROVED, либо REJECTED.
    //    Эндпоинт — PATCH /bookings/{bookingId}?approved={approved}, параметр approved может принимать значения true или false.
    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@RequestHeader(USER_ID_REQUEST_HEADER) long ownerId,
                                     @RequestPart(name = "bookingId") long bookingId,
                                     @RequestParam(name = "approved") boolean approved) {
        return null;
    }

    //    Получение данных о конкретном бронировании (включая его статус). Может быть выполнено либо автором бронирования, либо владельцем вещи, к которой относится бронирование.
    //    Эндпоинт — GET /bookings/{bookingId}.
    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader(USER_ID_REQUEST_HEADER) long userId, @RequestPart(name = "bookingId") long bookingId) {
        return null;
    }

    // ToDo
    // тут state (статус) интересный.
    // Некоторые состояния от BookingStatus, а другие свои.

    //    Получение списка всех бронирований текущего пользователя.
    //    Эндпоинт — GET /bookings?state={state}. Параметр state необязательный и по умолчанию равен ALL (англ. «все»).
    //    Также он может принимать значения CURRENT (англ. «текущие»), **PAST** (англ. «завершённые»), FUTURE (англ. «будущие»), WAITING (англ. «ожидающие подтверждения»), REJECTED (англ. «отклонённые»). Б
    //    ронирования должны возвращаться отсортированными по дате от более новых к более старым.
    @GetMapping
    public List<BookingDto> getBookingByState(@RequestParam(name = "state", defaultValue = "ALL") BookingStateForSearch state) {
        return null;
    }

    //    Получение списка бронирований для всех вещей текущего пользователя.
    //    Эндпоинт — GET /bookings/owner?state={state}. Этот запрос имеет смысл для владельца хотя бы одной вещи. Работа параметра state аналогична его работе в предыдущем сценарии.
    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookingsByState(@RequestHeader(USER_ID_REQUEST_HEADER) long ownerId, @RequestParam(name = "state", defaultValue = "ALL") BookingStateForSearch state) {
        return null;
    }
}
