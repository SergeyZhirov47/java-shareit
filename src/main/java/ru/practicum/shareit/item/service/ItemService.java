package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    Long create(ItemCreateDto item, long ownerId);

    ItemDto createAndGet(ItemCreateDto item, long ownerId);

    ItemDto update(long id, ItemDto item, long ownerId);

    ItemWithAdditionalDataDto getById(long id, long userId);

    ItemDto getOwnerItemById(long itemId, long ownerId);

    List<ItemWithAdditionalDataDto> getAllOwnerItems(long ownerId);

    List<ItemWithAdditionalDataDto> getAllOwnerItems(long ownerId, Integer from, Integer size);

    List<ItemDto> searchItems(String text, long userId);

    List<ItemDto> searchItems(String text, long userId, Integer from, Integer size);

    CommentDto addComment(long itemId, long userId, CommentCreateDto commentDto);
}
