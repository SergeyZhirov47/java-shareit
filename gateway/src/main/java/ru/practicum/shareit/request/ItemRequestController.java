package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.common.ConstantParamStorage.*;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    // добавить новый запрос вещи
    @PostMapping
    public ResponseEntity<Object> addItemRequest(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                                 @Valid @RequestBody ItemRequestCreateDto itemRequest) {
        log.info(String.format("POST /requests, body = %s, %s = %s", itemRequest, USER_ID_REQUEST_HEADER, userId));
        return itemRequestClient.createAndGet(itemRequest, userId);
    }

    // Получить список своих запросов вместе с данными об ответах на них
    @GetMapping
    public ResponseEntity<Object> getAllUserItemRequests(@RequestHeader(USER_ID_REQUEST_HEADER) long userId) {
        log.info(String.format("GET /requests, %s = %s", USER_ID_REQUEST_HEADER, userId));
        return itemRequestClient.getAllUserItemRequests(userId);
    }

    // Получить список запросов, созданных другими пользователями
    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                                     @PositiveOrZero @RequestParam(name = "from", defaultValue = DEFAULT_FROM_PARAM) Integer from,
                                                     @Positive @RequestParam(name = "size", defaultValue = DEFAULT_SIZE_PARAM) Integer size) {
        log.info(String.format("GET /requests/all?from={from}&size={size}, {from} = %s, {size} = %s, %s = %s", from, size, USER_ID_REQUEST_HEADER, userId));
        return itemRequestClient.getAllItemRequests(userId, from, size);
    }

    // Получить данные об одном конкретном запросе
    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequest(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                                 @PathVariable(name = "requestId") long requestId) {
        log.info(String.format("GET /requests/{requestId}, {requestId} = %s, %s = %s", requestId, USER_ID_REQUEST_HEADER, userId));
        return itemRequestClient.getItemRequestById(requestId, userId);
    }
}
