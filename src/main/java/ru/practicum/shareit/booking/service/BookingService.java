package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStateForSearch;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingCreateDto newBooking, long userId);

    BookingDto approve(long bookingId, long userId, boolean isApproved);

    BookingDto getBooking(long id, long userId);

    List<BookingDto> getUserBookingsByState(long userId, BookingStateForSearch searchState);

    List<BookingDto> getBookingsByItemOwner(long ownerId, BookingStateForSearch searchState);

    List<BookingDto> getUserBookingsByState(long userId, BookingStateForSearch searchState, Integer from, Integer size);

    List<BookingDto> getBookingsByItemOwner(long ownerId, BookingStateForSearch searchState, Integer from, Integer size);
}
