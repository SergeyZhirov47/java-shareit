package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
@Slf4j
public class ItemController {
    private static final String USER_ID_REQUEST_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    // Добавление вещи
    @PostMapping
    public ItemDto add(@RequestHeader(USER_ID_REQUEST_HEADER) long ownerId, @Valid @RequestBody ItemCreateDto item) {
        log.info(String.format("POST /items, body = %s, %s = %s", item, USER_ID_REQUEST_HEADER, ownerId));
        final ItemDto newItem = itemService.createAndGet(item, ownerId);
        log.info(String.format("Успешно добавлена вещь с id = %s", newItem.getId()));

        return newItem;
    }

    // Обновление информации о вещи ее владельцем
    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                          @Valid @RequestBody ItemDto item,
                          @PathVariable(name = "itemId") long itemId) {
        log.info(String.format("PATCH /items/{itemId}, body = %s, {itemId} = %s, %s = %s", item, itemId, USER_ID_REQUEST_HEADER, userId));
        final ItemDto updatedItem = itemService.update(itemId, item, userId);
        log.info(String.format("Успешно обновлены данные вещи с id = %s", updatedItem.getId()));

        return updatedItem;
    }

    // Получение информации о вещи пользователем
    @GetMapping("/{itemId}")
    public ItemWithBookingDto getItemById(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                          @PathVariable(name = "itemId") long itemId) {
        log.info(String.format("GET /items/{itemId}, {itemId} = %s, %s = %s", itemId, USER_ID_REQUEST_HEADER, userId));
        final ItemWithBookingDto item = itemService.getById(itemId, userId);
        log.info(String.format("Успешно получены данные о вещи с id = %s", item.getId()));

        return item;
    }

    // Просмотр владельцем списка всех его вещей с указанием названия и описания для каждой
    @GetMapping
    public List<ItemWithBookingDto> getAllOwnerItems(@RequestHeader(USER_ID_REQUEST_HEADER) long ownerId) {
        log.info(String.format("GET /items, %s = %s", USER_ID_REQUEST_HEADER, ownerId));
        final List<ItemWithBookingDto> ownerItems = itemService.getAllOwnerItems(ownerId);
        log.info(String.format("Успешно получены вещи (%s штук) пользователя с id = %s", ownerItems.size(), ownerId));

        return ownerItems;
    }

    // Поиск вещи потенциальным арендатором.
    // Пользователь передаёт в строке запроса текст, и система ищет вещи, содержащие этот текст в названии или описании
    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                     @RequestParam(name = "text") String text) {
        log.info(String.format("GET /items/search?text=text, text = %s, %s = %s", text, USER_ID_REQUEST_HEADER, userId));
        final List<ItemDto> searchedItems = itemService.searchItems(text, userId);
        log.info(String.format("Успешно получены вещи (%s штук) по запросу \"%s\" пользователя с id = %s", searchedItems.size(), text, userId));

        return searchedItems;
    }

    // Добавление комментария к вещи, которую когда-то бронировал.
    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                 @Valid @RequestBody CommentCreateDto comment,
                                 @PathVariable(name = "itemId") long itemId) {
        log.info(String.format("POST /items/{itemId}/comment, {itemId} = %s, %s = %s", itemId, USER_ID_REQUEST_HEADER, userId));
        final CommentDto commentDto = itemService.addComment(itemId, userId, comment);
        log.info(String.format("Комментарий успешно добавлен (id = %s)", commentDto.getId()));

        return commentDto;
    }
}
