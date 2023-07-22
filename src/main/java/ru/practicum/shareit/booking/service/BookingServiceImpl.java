package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStateForSearch;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.validation.BookingDatesValidator;
import ru.practicum.shareit.common.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        final User user = userRepository.getUserById(userId);

        // Проверяем, что вещь существует.
        final long itemId = newBooking.getItemId();
        final Item item = itemRepository.getItemById(itemId);

        // Валидация (начало и конец бронирования)
        validateBookingCreateDto(newBooking);

        // Проверяем, что пользователь не владелец вещи (нелогично у самого себя бронировать вещь).
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не может бронировать собственные вещи");
        }

        // Проверяем, что вещь доступна.
        if (!item.isAvailable()) {
            throw new UnsupportedOperationException(String.format("Вещь с id = %s недоступна для бронирования!", itemId));
        }

        Booking booking = BookingMapper.toBooking(newBooking);
        booking.setStatus(BookingStatus.WAITING);
        booking.setBooker(user);
        booking.setItem(item);

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
            throw new UnsupportedOperationException(String.format("При подтверждении/отклонении заявки ее статус должен быть %s. Текущий статус - %s", BookingStatus.WAITING, booking.getStatus()));
        }

        // Проверяем, что одобрение/отклонение устанавливает владелец вещи.
        if (!bookingRepository.isUserOwnItemFromBooking(bookingId, userId)) {
            throw new NotFoundException(String.format("Статус заявки на бронирование вещи может менять только ее владелец! (id заявки = %s, id пользователя = %s)", bookingId, userId));
        }

        final BookingStatus newStatus = isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(newStatus);

        booking = bookingRepository.save(booking);

        return BookingMapper.toBookingDto(booking);
    }

    // Получение данных о конкретном бронировании (включая его статус).
    // Может быть выполнено либо автором бронирования, либо владельцем вещи, к которой относится бронирование.
    @Override
    public BookingDto getBooking(long id, long userId) {
        // Проверяем существует ли пользователь.
        userRepository.checkUserExists(userId);

        // Проверяем есть ли заявка на бронирование.
        final Booking booking = getBooking(id);

        // Проверяем есть ли доступ (запросил автор или владелец).
        final boolean isUserOwnItem = bookingRepository.isUserOwnItemFromBooking(id, userId);
        final boolean isUserAuthorBooking = bookingRepository.isUserBookingAuthor(id, userId);
        if (!isUserOwnItem && !isUserAuthorBooking) {
            throw new NotFoundException(String.format("Данные о бронировании может запросить либо владелец вещи либо автор бронирования. Пользователь id = %s не подходит под эти требования", userId));
        }

        return BookingMapper.toBookingDto(booking);
    }

    // Получение списка всех бронирований текущего пользователя (т.е список всех заявок на бронирование созданных данным пользователем).
    // Бронирования должны возвращаться отсортированными по дате от более новых к более старым.
    @Override
    public List<BookingDto> getUserBookingsByState(long userId, BookingStateForSearch searchState) {
        // Проверяем существует ли пользователь.
        userRepository.checkUserExists(userId);

        final List<Booking> userBookings = bookingRepository.getUserBookingsByState(userId, searchState);

        final List<BookingDto> userBookingsDto = userBookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toUnmodifiableList());

        return userBookingsDto;
    }

    // Получение списка бронирований для всех вещей текущего пользователя. (т.е все заявки на бронирование вещей данного пользователя).
    // Этот запрос имеет смысл для владельца хотя бы одной вещи.
    // Работа параметра state аналогична его работе в предыдущем сценарии.
    @Override
    public List<BookingDto> getBookingsByItemOwner(long ownerId, BookingStateForSearch searchState) {
        // Проверяем существует ли пользователь.
        userRepository.checkUserExists(ownerId);

        final List<Booking> bookingsByOwner = bookingRepository.getBookingsByItemOwner(ownerId, searchState);
        final List<BookingDto> bookingsByOwnerDto = bookingsByOwner.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toUnmodifiableList());

        return bookingsByOwnerDto;
    }

    private void validateBookingCreateDto(BookingCreateDto bookingCreateDto) {
        final LocalDateTime start = bookingCreateDto.getStart();
        final LocalDateTime end = bookingCreateDto.getEnd();

        BookingDatesValidator.validate(start, end);
    }

    private Booking getBooking(long bookingId) {
        final Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);

        if (bookingOpt.isEmpty()) {
            throw new BookingNotFoundException(bookingId);
        }

        return bookingOpt.get();
    }
}
