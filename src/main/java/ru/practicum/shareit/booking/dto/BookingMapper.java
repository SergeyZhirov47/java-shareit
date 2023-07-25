package ru.practicum.shareit.booking.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.common.AbstractMapper;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.dto.UserMapper;

import static java.util.Objects.isNull;

@UtilityClass
public class BookingMapper extends AbstractMapper {
    public BookingDto toBookingDto(Booking booking) {
        if (isNull(booking)) return null;

        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(ItemMapper.toItemDto(booking.getItem()))
                .booker(UserMapper.toUserDto(booking.getBooker()))
                .status(booking.getStatus())
                .build();
    }

    public BookingForItemDto toBookingForItemDto(Booking booking) {
        if (isNull(booking)) return null;

        return BookingForItemDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .bookerId(booking.getBooker().getId())
                .build();
    }

    public Booking toBooking(BookingCreateDto bookingCreateDto) {
        if (isNull(bookingCreateDto)) return null;

        return Booking.builder()
                .start(bookingCreateDto.getStart())
                .end(bookingCreateDto.getEnd())
                .build();
    }
}
