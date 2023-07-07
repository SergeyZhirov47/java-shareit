package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
@Slf4j
public class ItemController {
    private final static String USER_ID_REQUEST_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ItemDto add(@RequestHeader(USER_ID_REQUEST_HEADER) long ownerId, @Valid @RequestBody ItemCreateDto item) {
        log.info(String.format("POST /items, body = %s, %s = %s", item, USER_ID_REQUEST_HEADER, ownerId));
        return itemService.createAndGet(item, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                          @Valid @RequestBody ItemDto item,
                          @PathVariable(name = "itemId") long itemId) {
        log.info(String.format("PATCH /items/{itemId}, body = %s, {itemId} = %s, %s = %s", item, itemId, USER_ID_REQUEST_HEADER, userId));
        return itemService.update(itemId, item, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                          @PathVariable(name = "itemId") long itemId) {
        log.info(String.format("GET /items/{itemId}, {itemId} = %s, %s = %s", itemId, USER_ID_REQUEST_HEADER, userId));
        return itemService.getById(itemId);
    }

    // Просмотр владельцем списка всех его вещей с указанием названия и описания для каждой
    @GetMapping
    public List<ItemDto> getAllOwnerItems(@RequestHeader(USER_ID_REQUEST_HEADER) long ownerId) {
        log.info(String.format("GET /items, %s = %s", USER_ID_REQUEST_HEADER, ownerId));
        return itemService.getAllOwnerItems(ownerId);
    }

    // Поиск вещи потенциальным арендатором.
    // Пользователь передаёт в строке запроса текст, и система ищет вещи, содержащие этот текст в названии или описании
    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                     @NotBlank @RequestParam(name = "text") String text) {
        log.info(String.format("GET /items/search?text=text, text = %s, %s = %s", text, USER_ID_REQUEST_HEADER, userId));
        return itemService.searchItems(text, userId);
    }
}
