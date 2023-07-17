package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    //  Добавление нового запроса на бронирование.
    //  Запрос может быть создан любым пользователем, а затем подтверждён владельцем вещи.
    //  После создания запрос находится в статусе WAITING — «ожидает подтверждения».
    @Override
    public BookingDto create(BookingCreateDto newBooking, long userId) {
        // Проверяем, что пользователь существует.
        final User user = getUser(userId);

        // Проверяем, что вещь существует.
        final long itemId = newBooking.getItemId();
        final Item item = getItem(itemId);

        // Валидация (начало и конец бронирования)
        validateBookingCreateDto(newBooking);

        // Проверяем, что вещь доступна.
        if (!item.isAvailable()) {
            // ToDo
            // придумать исключение для этого случая.
            throw new UnsupportedOperationException(String.format("Вещь с id = %s недоступна для бронирования!", itemId));
        }

        Booking booking = BookingMapper.toBooking(newBooking);
        booking.setStatus(BookingStatus.WAITING);
        booking.setBooker(user);
        booking.setItem(item);

        // ToDo
        // Тогда что за сущность и таблица Requests. Какую роль она играет?

        booking = bookingRepository.save(booking);

        return BookingMapper.toBookingDto(booking);
    }

    // Подтверждение или отклонение запроса на бронирование.
    // Может быть выполнено только владельцем вещи.
    // Затем статус бронирования становится либо APPROVED, либо REJECTED
    @Override
    public BookingDto approve(long bookingId, long userId, boolean isApproved) {
        // Проверяем есть ли заявка на бронирование.
        Booking booking = getBooking(bookingId);

        if (booking.getStatus() != BookingStatus.WAITING) {
            // ToDo
            // придумать исключение для этого случая.
            throw new UnsupportedOperationException(String.format("При подтверждении/отклонении заявки ее статус должен быть %s. Текущий статус - %s", BookingStatus.WAITING, booking.getStatus()));
        }

        // Проверяем, что одобрение/отклонение устанавливает владелец вещи.
        if (!bookingRepository.isUserOwnItemFromBooking(bookingId, userId)) {
            // ToDo
            // придумать исключение для этого случая.
            // ToDo
            // нужны данные заявки и пользователя?
            throw new UnsupportedOperationException("Статус заявки на бронирование вещи может менять только ее владелец! (id заявки = %s, id пользователя = %s)");
        }

        final BookingStatus newStatus = isApproved ? BookingStatus.APPROVED : BookingStatus.CANCELED;
        booking.setStatus(newStatus);

        booking = bookingRepository.save(booking);

        return BookingMapper.toBookingDto(booking);
    }

    // ToDo
    // где эта логика должна быть? В моделе? тогда как dto получат к ней доступ?
    // в сервисе? тогда в моделе вообще какая-то логика может быть?
    private void validateBookingCreateDto(BookingCreateDto bookingCreateDto) {
        final LocalDateTime start = bookingCreateDto.getStart();
        final LocalDateTime end = bookingCreateDto.getEnd();

        if (start.equals(end)) {
            throw new ValidationException("Дата начала бронирования не может быть равна дате конца бронирования!");
        }

        if (end.isBefore(start)) {
            throw new ValidationException("Дата конца бронирования не может быть раньше даты начала бронирования!");
        }
    }

    // ToDo
    // Практически один и тот же код... Как-то можно упростить?
    private Booking getBooking(long bookingId) {
        final Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);

        if (bookingOpt.isEmpty()) {
            throw new BookingNotFoundException(bookingId);
        }

        return bookingOpt.get();
    }

    private User getUser(long userId) {
        final Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        return userOpt.get();
    }

    private Item getItem(long itemId) {
        final Optional<Item> itemOpt = itemRepository.findById(itemId);

        if (itemOpt.isEmpty()) {
            throw new ItemNotFoundException(itemId);
        }

        return itemOpt.get();
    }
}
