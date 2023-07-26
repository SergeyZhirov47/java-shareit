package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

public interface ItemCustomRepository {
    Item getItemById(long id);

    void checkItemExists(long id);

    Item getByIdAndOwnerId(long id, long ownerId);
}
