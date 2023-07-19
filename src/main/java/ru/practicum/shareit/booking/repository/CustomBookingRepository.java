package ru.practicum.shareit.booking.repository;

import ru.practicum.shareit.booking.model.Booking;

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
}
