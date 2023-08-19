package ru.practicum.shareit.booking.validation;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.model.BookingStateForSearch;
import ru.practicum.shareit.common.ValidationException;

import static java.util.Objects.isNull;

@UtilityClass
public class BookingStateForSearchHelper {
    public BookingStateForSearch validateAndGet(final String stateStr) {
        BookingStateForSearch state;
        try {
            state = convertFromString(stateStr);
        } catch (IllegalArgumentException exp) {
            throw new ValidationException(String.format("Unknown state: %s", stateStr));
        }

        return state;
    }

    public BookingStateForSearch convertFromString(final String stateStr) {
        if (isNull(stateStr)) {
            return BookingStateForSearch.ALL;
        }

        return BookingStateForSearch.valueOf(stateStr);
    }
}
