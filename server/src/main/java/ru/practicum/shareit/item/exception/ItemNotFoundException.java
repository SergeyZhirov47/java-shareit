package ru.practicum.shareit.item.exception;

import ru.practicum.shareit.common.NotFoundException;

public class ItemNotFoundException extends NotFoundException {
    private static final String MESSAGE_BASE = "Вещь с id = %s не найдена";

    public ItemNotFoundException(long id) {
        super(String.format(MESSAGE_BASE, id));
    }
}
