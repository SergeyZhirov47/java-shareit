package ru.practicum.shareit.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.validation.ValidationException;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler {
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseData> handle(ValidationException exp) {
        log.warn(exp.getMessage(), exp);
        return new ResponseEntity<>(new ErrorResponseData(exp.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
