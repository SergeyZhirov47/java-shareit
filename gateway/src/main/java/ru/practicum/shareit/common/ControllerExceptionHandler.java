package ru.practicum.shareit.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.practicum.shareit.validation.ValidationException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler {
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseData> handle(ValidationException exp) {
        log.warn(exp.getMessage(), exp);
        return new ResponseEntity<>(new ErrorResponseData(exp.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponseData> handle(UnsupportedOperationException exp) {
        log.error(exp.getMessage(), exp);
        return new ResponseEntity<>(new ErrorResponseData(exp.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponseData> handle(Throwable exp) {
        log.error(exp.getMessage(), exp);
        return new ResponseEntity<>(new ErrorResponseData("internal server error. info: " + exp.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handle(MethodArgumentNotValidException exp) {
        log.warn(exp.getMessage(), exp);

        final Map<String, String> errorMessageMap = new HashMap<>();
        exp.getBindingResult().getFieldErrors().forEach(error -> errorMessageMap.put(error.getField(), error.getDefaultMessage()));

        return new ResponseEntity<>(errorMessageMap, HttpStatus.BAD_REQUEST);
    }
}
