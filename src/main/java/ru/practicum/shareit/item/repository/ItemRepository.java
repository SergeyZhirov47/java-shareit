package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Optional<Item> getById(long id);

    Optional<Item> getByIdAndOwnerId(long id, long ownerId);

    List<Item> getOwnerItems(long ownerId);

    Long add(Item item, long ownerId);

    Item update(Item item, long ownerId);

    List<Item> search(String text, long userId);

    boolean checkUserOwnItem(long userId, long itemId);
}
