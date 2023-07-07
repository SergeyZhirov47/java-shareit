package ru.practicum.shareit.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.item.exception.NotOwnerAccessException;
import ru.practicum.shareit.user.exception.EmailAlreadyUsedException;

@Slf4j
@RestControllerAdvice
public class ExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseData handle(NotFoundException exp) {
        return simpleHandle(exp);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(EmailAlreadyUsedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseData handle(EmailAlreadyUsedException exp) {
        return simpleHandle(exp);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(NotOwnerAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponseData handle(NotOwnerAccessException exp) {
        return simpleHandle(exp);
    }

    private ErrorResponseData simpleHandle(RuntimeException exp) {
        log.warn(exp.getMessage(), exp);
        return new ErrorResponseData(exp.getMessage());
    }
}
