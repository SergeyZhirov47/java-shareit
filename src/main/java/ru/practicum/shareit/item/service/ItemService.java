package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    Long create(ItemCreateDto item, long ownerId);

    ItemDto createAndGet(ItemCreateDto item, long ownerId);

    ItemDto update(long id, ItemDto item, long ownerId);

    ItemWithBookingDto getById(long id, long userId);

    ItemDto getOwnerItemById(long itemId, long ownerId);

    List<ItemWithBookingDto> getAllOwnerItems(long ownerId);

    List<ItemDto> searchItems(String text, long userId);

    CommentDto addComment(long itemId, long userId, CommentCreateDto commentDto);
}
