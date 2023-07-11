package ru.practicum.shareit.item.exception;

public class NotOwnerAccessException extends RuntimeException {
    public NotOwnerAccessException(String message) {
        super(message);
    }
}
