package ru.practicum.shareit.booking.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStateForSearch;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.QBooking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.NotFoundException;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Objects.nonNull;

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

        // Проверяем, что пользователь не владелец вещи (не логично у самого себя бронировать вещь).
        if (item.getOwner().getId().equals(userId)) {
            // ToDo
            // Почему 404 должно возвращать я не понимаю.
            throw new NotFoundException("Пользователь не может бронировать собственные вещи");
        }

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
        checkUserExists(userId);

        // Проверяем есть ли заявка на бронирование.
        final Booking booking = getBooking(id);

        // Проверяем есть ли доступ (запросил автор или владелец).
        final boolean isUserOwnItem = bookingRepository.isUserOwnItemFromBooking(id, userId);
        final boolean isUserAuthorBooking = bookingRepository.isUserBookingAuthor(id, userId);
        if (!isUserOwnItem && !isUserAuthorBooking) {
            // ToDo
            // Почему 404 тут должна быть?
            throw new NotFoundException(String.format("Данные о бронировании может запросить либо владелец вещи либо автор бронирования. Пользователь id = %s не подходит под эти требования", userId));
        }

        return BookingMapper.toBookingDto(booking);
    }

    // ToDo
    // в этих методах много похожего... можно ли вынести в общий метод/методы?

    // Получение списка всех бронирований текущего пользователя (т.е список всех заявок на бронирование созданных данным пользователем).
    // Бронирования должны возвращаться отсортированными по дате от более новых к более старым.
    @Override
    public List<BookingDto> getUserBookingsByState(long userId, BookingStateForSearch searchState) {
        // Проверяем существует ли пользователь.
        checkUserExists(userId);

        // Все заявки на бронирование, созданные пользователем.
        final BooleanExpression userBookingsExpression = QBooking.booking.booker.id.eq(userId);

        // условие сформированное исходя из searchState.
        final BooleanExpression searchStateExpression = getSearchExpressionByState(searchState);
        if (nonNull(searchStateExpression)) {
            userBookingsExpression.and(searchStateExpression);
        }

        final Iterable<Booking> userBookings = bookingRepository.findAll(userBookingsExpression, Sort.by(Sort.Direction.DESC, "start"));
        final List<BookingDto> userBookingsDto = StreamSupport.stream(userBookings.spliterator(), false)
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
        checkUserExists(ownerId);

        // Все заявки на бронирование вещей данного пользователя.
        final BooleanExpression bookingsByItemsOwnerExpression = QBooking.booking.item.owner.id.eq(ownerId);

        // условие сформированное исходя из searchState.
        final BooleanExpression searchStateExpression = getSearchExpressionByState(searchState);
        if (nonNull(searchStateExpression)) {
            bookingsByItemsOwnerExpression.and(searchStateExpression);
        }

        final Iterable<Booking> bookingsByOwner = bookingRepository.findAll(bookingsByItemsOwnerExpression, Sort.by(Sort.Direction.DESC, "start"));
        final List<BookingDto> bookingsByOwnerDto = StreamSupport.stream(bookingsByOwner.spliterator(), false)
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toUnmodifiableList());

        return bookingsByOwnerDto;
    }

    private BooleanExpression getSearchExpressionByState(BookingStateForSearch searchState) {
        BooleanExpression searchStateExpression = null;
        final LocalDateTime now = LocalDateTime.now();

        switch (searchState) {
            case PAST: {
                searchStateExpression = QBooking.booking.end.before(now);
                break;
            }
            case FUTURE: {
                searchStateExpression = QBooking.booking.start.after(now);
                break;
            }
            case CURRENT: {
                searchStateExpression = QBooking.booking.start.before(now).and(QBooking.booking.end.after(now));
                break;
            }
            case WAITING: {
                searchStateExpression = QBooking.booking.status.eq(BookingStatus.WAITING);
                break;
            }
            case REJECTED: {
                searchStateExpression = QBooking.booking.status.eq(BookingStatus.REJECTED);
                break;
            }
        }

        return searchStateExpression;
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

    private void checkUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }

    private Item getItem(long itemId) {
        final Optional<Item> itemOpt = itemRepository.findById(itemId);

        if (itemOpt.isEmpty()) {
            throw new ItemNotFoundException(itemId);
        }

        return itemOpt.get();
    }
}
