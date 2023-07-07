package ru.practicum.shareit.user.exception;

import ru.practicum.shareit.common.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    private static final String MESSAGE_BASE = "Пользователь с id = %s не найден";

    public UserNotFoundException(long id) {
        super(String.format(MESSAGE_BASE, id));
    }
}
