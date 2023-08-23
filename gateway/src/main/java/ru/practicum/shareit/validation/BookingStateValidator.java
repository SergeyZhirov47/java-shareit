package ru.practicum.shareit.validation;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingState;

import static java.util.Objects.isNull;

@UtilityClass
public class BookingStateValidator {
    public BookingState validateAndGet(final String stateStr) {
        if (isNull(stateStr)) {
            return BookingState.ALL;
        }

        BookingState state;
        try {
            state = BookingState.valueOf(stateStr);
        } catch (IllegalArgumentException exp) {
            throw new ValidationException(String.format("Unknown state: %s", stateStr));
        }

        return state;
    }
}
