package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

public interface BookingService {
    BookingDto create(BookingCreateDto newBooking);
}
