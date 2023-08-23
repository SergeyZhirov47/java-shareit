package ru.practicum.shareit.request.exception;

import ru.practicum.shareit.common.NotFoundException;

public class ItemRequestNotFoundException extends NotFoundException {
    private static final String MESSAGE_BASE = "Запрос на вещь с id = %s не найден";

    public ItemRequestNotFoundException(long id) {
        super(String.format(MESSAGE_BASE, id));
    }
}
