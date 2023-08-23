package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.common.ConstantParamStorage.*;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    // Добавление вещи
    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(USER_ID_REQUEST_HEADER) long ownerId, @Valid @RequestBody ItemCreateDto item) {
        log.info(String.format("POST /items, body = %s, %s = %s", item, USER_ID_REQUEST_HEADER, ownerId));
        return itemClient.createAndGet(item, ownerId);
    }

    // Обновление информации о вещи ее владельцем
    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                         @Valid @RequestBody ItemDto item,
                                         @PathVariable(name = "itemId") long itemId) {
        log.info(String.format("PATCH /items/{itemId}, body = %s, {itemId} = %s, %s = %s", item, itemId, USER_ID_REQUEST_HEADER, userId));
        return itemClient.update(itemId, item, userId);
    }

    // Получение информации о вещи пользователем
    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                              @PathVariable(name = "itemId") long itemId) {
        log.info(String.format("GET /items/{itemId}, {itemId} = %s, %s = %s", itemId, USER_ID_REQUEST_HEADER, userId));
        return itemClient.getById(itemId, userId);
    }

    // Просмотр владельцем списка всех его вещей с указанием названия и описания для каждой
    @GetMapping
    public ResponseEntity<Object> getAllOwnerItems(@RequestHeader(USER_ID_REQUEST_HEADER) long ownerId,
                                                   @PositiveOrZero @RequestParam(name = "from", defaultValue = DEFAULT_FROM_PARAM) Integer from,
                                                   @Positive @RequestParam(name = "size", defaultValue = DEFAULT_SIZE_PARAM) Integer size) {
        log.info(String.format("GET /items?from={from}&size={size}, {from} = %s, {size} = %s, %s = %s", from, size, USER_ID_REQUEST_HEADER, ownerId));
        return itemClient.getAllOwnerItems(ownerId, from, size);
    }

    // Поиск вещи потенциальным арендатором.
    // Пользователь передаёт в строке запроса текст, и система ищет вещи, содержащие этот текст в названии или описании
    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                              @RequestParam(name = "text") String text,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = DEFAULT_FROM_PARAM) Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = DEFAULT_SIZE_PARAM) Integer size) {
        final String logStr = "GET /items/search?text=text&from={from}&size={size}, text = %s, {from} = %s, {size} = %s, %s = %s";
        log.info(String.format(logStr, text, from, size, USER_ID_REQUEST_HEADER, userId));
        return itemClient.searchItems(text, userId, from, size);
    }

    // Добавление комментария к вещи, которую когда-то бронировал.
    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                             @Valid @RequestBody CommentCreateDto comment,
                                             @PathVariable(name = "itemId") long itemId) {
        log.info(String.format("POST /items/{itemId}/comment, {itemId} = %s, %s = %s", itemId, USER_ID_REQUEST_HEADER, userId));
        return itemClient.addComment(itemId, userId, comment);
    }
}
