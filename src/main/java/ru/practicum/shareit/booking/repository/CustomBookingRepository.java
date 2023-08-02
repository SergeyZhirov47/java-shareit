package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStateForSearch;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CustomBookingRepository {
    Booking getLastBookingForItemById(long itemId, LocalDateTime endDate);

    Booking getNextBookingForItemById(long itemId, LocalDateTime startDate);

    // Ключ - id предмета, значения - заявка на бронирование (недавнее)
    Map<Long, Booking> getLastBookingForItemsByIdList(List<Long> itemIdList, LocalDateTime endDate);

    // Ключ - id предмета, значения - заявка на бронирование (следующее)
    Map<Long, Booking> getNextBookingForItemsByIdList(List<Long> itemIdList, LocalDateTime startDate);

    boolean isUserBookingItem(long userId, long itemId, LocalDateTime startUsingBeforeDate);

    List<Booking> getUserBookingsByState(long userId, BookingStateForSearch searchState);

    List<Booking> getBookingsByItemOwner(long ownerId, BookingStateForSearch searchState);

    List<Booking> getUserBookingsByState(long userId, BookingStateForSearch searchState, Pageable pageable);

    List<Booking> getBookingsByItemOwner(long ownerId, BookingStateForSearch searchState, Pageable pageable);
}
