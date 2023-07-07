package ru.practicum.shareit.item;

public class NotOwnerAccessException extends RuntimeException {
    public NotOwnerAccessException(String message) {
        super(message);
    }
}
