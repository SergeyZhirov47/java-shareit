package ru.practicum.shareit.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.item.exception.NotOwnerAccessException;
import ru.practicum.shareit.user.exception.EmailAlreadyUsedException;

import java.util.HashMap;
import java.util.Map;

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

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handle(MethodArgumentNotValidException exp) {
        log.warn(exp.getMessage(), exp);

        final Map<String, String> errorMessageMap = new HashMap<>();
        exp.getBindingResult().getFieldErrors().forEach(error -> errorMessageMap.put(error.getField(), error.getDefaultMessage()));

        return errorMessageMap;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseData handle(Throwable exp) {
        log.error(exp.getMessage(), exp);

        return new ErrorResponseData("internal server error. info: " + exp.getMessage());
    }

    private ErrorResponseData simpleHandle(RuntimeException exp) {
        log.warn(exp.getMessage(), exp);
        return new ErrorResponseData(exp.getMessage());
    }
}
