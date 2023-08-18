package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.shareit.common.ConstantParamStorage.USER_ID_REQUEST_HEADER;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/requests")
@Slf4j
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    // добавить новый запрос вещи
    @PostMapping
    public ItemRequestDto addItemRequest(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                         @Valid @RequestBody ItemRequestCreateDto itemRequest) {
        log.info(String.format("POST /requests, body = %s, %s = %s", itemRequest, USER_ID_REQUEST_HEADER, userId));
        final ItemRequestDto newItemRequest = itemRequestService.createAndGet(itemRequest, userId);
        log.info(String.format("Успешно создан запрос на вещь. Id запроса = %s", newItemRequest.getId()));

        return newItemRequest;
    }

    // Получить список своих запросов вместе с данными об ответах на них
    @GetMapping
    public List<ItemRequestDto> getAllUserItemRequests(@RequestHeader(USER_ID_REQUEST_HEADER) long userId) {
        log.info(String.format("GET /requests, %s = %s", USER_ID_REQUEST_HEADER, userId));
        final List<ItemRequestDto> userItemRequests = itemRequestService.getAllUserItemRequests(userId);
        log.info(String.format("Успешно получены заявки на вещи от пользователя с id = %s", userId));

        return userItemRequests;
    }

    // Получить список запросов, созданных другими пользователями
    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequests(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                                   @PositiveOrZero @RequestParam(name = "from", required = false) Integer from,
                                                   @Positive @RequestParam(name = "size", required = false) Integer size) {
        log.info(String.format("GET /requests/all?from={from}&size={size}, {from} = %s, {size} = %s, %s = %s", from, size, USER_ID_REQUEST_HEADER, userId));
        final List<ItemRequestDto> itemRequests = itemRequestService.getAllItemRequests(userId, from, size);
        log.info(String.format("Успешно получены заявки на вещи. Их кол-во %s", itemRequests.size()));

        return itemRequests;
    }

    // Получить данные об одном конкретном запросе
    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequest(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                         @PathVariable(name = "requestId") long requestId) {
        log.info(String.format("GET /requests/{requestId}, {requestId} = %s, %s = %s", requestId, USER_ID_REQUEST_HEADER, userId));
        final ItemRequestDto itemRequestDto = itemRequestService.getItemRequestById(requestId, userId);
        log.info(String.format("Успешно получен заявка на вещь. Id заявки = %S", itemRequestDto.getId()));

        return itemRequestDto;
    }
}
