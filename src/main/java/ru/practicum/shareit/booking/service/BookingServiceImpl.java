package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

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
        // После этого у вещи статус становится isAvailable = false
        // или после утверждения заявки?
        // Или это вообще другое. Похоже что другое. Можно сколько угодно создать заявок на бронирование пока вещь доступна для бронирования.

        // ToDo
        // Тогда что за сущность и таблица Requests. Какую роль она играет?

        booking = bookingRepository.save(booking);

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto approve(long bookingId, long itemOwnerId, boolean isApproved) {
        // Проверяем есть ли заявка на бронирование.

        // Проверяем, что статус устанавливаем владелец вещи.
        // - получаем вещь, а у вещи пользователя и сравниваем Id. Минус - тянем всю инфу по вещи и по пользователю.
        // - запрос (sql или джава... суть таже).

        if (isApproved) {

        }

        return null;
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
