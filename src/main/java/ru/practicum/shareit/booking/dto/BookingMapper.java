package ru.practicum.shareit.booking.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.common.AbstractMapper;

@UtilityClass
public class BookingMapper extends AbstractMapper {
    public BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(booking.getItem())
                .booker(booking.getBooker())
                .status(booking.getStatus())
                .build();
    }

    public Booking toBooking(BookingCreateDto bookingCreateDto) {
        return Booking.builder()
                .start(bookingCreateDto.getStart())
                .end(bookingCreateDto.getEnd())
                .build();
    }

    public Booking updateIfDifferent(final Booking booking, final BookingDto bookingWithChanges) {
        return Booking.builder()
                .id(booking.getId())
                .item(booking.getItem())
                .booker(booking.getBooker())
                .start(getChanged(booking.getStart(), bookingWithChanges.getStart()))
                .end(getChanged(booking.getEnd(), bookingWithChanges.getEnd()))
                .status(getChanged(booking.getStatus(), bookingWithChanges.getStatus()))
                .build();
    }
}