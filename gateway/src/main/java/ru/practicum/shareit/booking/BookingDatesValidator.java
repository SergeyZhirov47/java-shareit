package ru.practicum.shareit.booking;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.validation.ValidationException;

import java.time.LocalDateTime;

@UtilityClass
public class BookingDatesValidator {
    public void validate(LocalDateTime start, LocalDateTime end) {
        if (start.equals(end)) {
            throw new ValidationException("Дата начала бронирования не может быть равна дате конца бронирования!");
        }

        if (end.isBefore(start)) {
            throw new ValidationException("Дата конца бронирования не может быть раньше даты начала бронирования!");
        }
    }

    public void validate(BookingCreateDto bookingCreateDto) {
        validate(bookingCreateDto.getStart(), bookingCreateDto.getEnd());
    }
}
