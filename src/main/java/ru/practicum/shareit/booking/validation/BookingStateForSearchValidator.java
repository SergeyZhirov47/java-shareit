package ru.practicum.shareit.booking.validation;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.model.BookingStateForSearch;
import ru.practicum.shareit.common.ValidationException;

import static java.util.Objects.isNull;

@UtilityClass
public class BookingStateForSearchValidator {
    public BookingStateForSearch validateAndGet(final String stateStr) {
        if (isNull(stateStr)) {
            return BookingStateForSearch.ALL;
        }

        BookingStateForSearch state;
        try {
            state = BookingStateForSearch.valueOf(stateStr);
        } catch (IllegalArgumentException exp) {
            throw new ValidationException(String.format("Unknown state: %s", stateStr));
        }

        return state;
    }
}
