package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static ru.practicum.shareit.common.ConstantParamStorage.USER_ID_REQUEST_HEADER;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;

    // Добавление вещи
    @PostMapping
    public ItemDto add(@RequestHeader(USER_ID_REQUEST_HEADER) long ownerId, @RequestBody ItemCreateDto item) {
        log.info(String.format("POST /items, body = %s, %s = %s", item, USER_ID_REQUEST_HEADER, ownerId));
        final ItemDto newItem = itemService.createAndGet(item, ownerId);
        log.info(String.format("Успешно добавлена вещь с id = %s", newItem.getId()));

        return newItem;
    }

    // Обновление информации о вещи ее владельцем
    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                          @RequestBody ItemDto item,
                          @PathVariable(name = "itemId") long itemId) {
        log.info(String.format("PATCH /items/{itemId}, body = %s, {itemId} = %s, %s = %s", item, itemId, USER_ID_REQUEST_HEADER, userId));
        final ItemDto updatedItem = itemService.update(itemId, item, userId);
        log.info(String.format("Успешно обновлены данные вещи с id = %s", updatedItem.getId()));

        return updatedItem;
    }

    // Получение информации о вещи пользователем
    @GetMapping("/{itemId}")
    public ItemWithAdditionalDataDto getItemById(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                                 @PathVariable(name = "itemId") long itemId) {
        log.info(String.format("GET /items/{itemId}, {itemId} = %s, %s = %s", itemId, USER_ID_REQUEST_HEADER, userId));
        final ItemWithAdditionalDataDto item = itemService.getById(itemId, userId);
        log.info(String.format("Успешно получены данные о вещи с id = %s", item.getId()));

        return item;
    }

    // Просмотр владельцем списка всех его вещей с указанием названия и описания для каждой
    @GetMapping
    public List<ItemWithAdditionalDataDto> getAllOwnerItems(@RequestHeader(USER_ID_REQUEST_HEADER) long ownerId,
                                                            @RequestParam(name = "from", required = false) Integer from,
                                                            @RequestParam(name = "size", required = false) Integer size) {
        log.info(String.format("GET /items?from={from}&size={size}, {from} = %s, {size} = %s, %s = %s", from, size, USER_ID_REQUEST_HEADER, ownerId));
        final List<ItemWithAdditionalDataDto> ownerItems = itemService.getAllOwnerItems(ownerId, from, size);
        log.info(String.format("Успешно получены вещи (%s штук) пользователя с id = %s", ownerItems.size(), ownerId));

        return ownerItems;
    }

    // Поиск вещи потенциальным арендатором.
    // Пользователь передаёт в строке запроса текст, и система ищет вещи, содержащие этот текст в названии или описании
    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                     @RequestParam(name = "text") String text,
                                     @RequestParam(name = "from", required = false) Integer from,
                                     @RequestParam(name = "size", required = false) Integer size) {
        final String logStr = "GET /items/search?text={text}&from={from}&size={size}, {text} = %s, {from} = %s, {size} = %s, %s = %s";
        log.info(String.format(logStr, text, from, size, USER_ID_REQUEST_HEADER, userId));
        final List<ItemDto> searchedItems = itemService.searchItems(text, userId, from, size);
        log.info(String.format("Успешно получены вещи (%s штук) по запросу \"%s\" пользователя с id = %s", searchedItems.size(), text, userId));

        return searchedItems;
    }

    // Добавление комментария к вещи, которую когда-то бронировал.
    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                 @RequestBody CommentCreateDto comment,
                                 @PathVariable(name = "itemId") long itemId) {
        log.info(String.format("POST /items/{itemId}/comment, {itemId} = %s, %s = %s", itemId, USER_ID_REQUEST_HEADER, userId));
        final CommentDto commentDto = itemService.addComment(itemId, userId, comment);
        log.info(String.format("Комментарий успешно добавлен (id = %s)", commentDto.getId()));

        return commentDto;
    }
}
