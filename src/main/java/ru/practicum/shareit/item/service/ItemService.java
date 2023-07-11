package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    Long create(ItemCreateDto item, long ownerId);

    ItemDto createAndGet(ItemCreateDto item, long ownerId);

    ItemDto update(long id, ItemDto item, long ownerId);

    ItemDto getById(long id);

    ItemDto getOwnerItemById(long itemId, long ownerId);

    List<ItemDto> getAllOwnerItems(long ownerId);

    List<ItemDto> searchItems(String text, long userId);
}
