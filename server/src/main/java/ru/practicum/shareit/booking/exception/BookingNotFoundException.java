package ru.practicum.shareit.booking.exception;

import ru.practicum.shareit.common.NotFoundException;

public class BookingNotFoundException extends NotFoundException {
    private static final String MESSAGE_BASE = "Заявка на бронирование с id = %s не найдена";

    public BookingNotFoundException(long id) {
        super(String.format(MESSAGE_BASE, id));
    }
}
