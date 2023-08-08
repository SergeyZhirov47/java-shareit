package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface DaoItem {
    List<Item> findByOwnerId(long ownerId);

    List<Item> findByOwnerId(long ownerId, Pageable pageable);

    boolean existsByIdAndOwnerId(long itemId, long userId);

    Item getByIdAndOwnerId(long itemId, long ownerId);

    List<Item> findAvailableByNameOrDescription(String text);

    List<Item> findAvailableByNameOrDescription(String text, Pageable pageable);

    Item getItemById(long id);

    boolean existsById(long id);

    void checkItemExists(long id);

    Item save(Item entity);

    List<Item> findAll();

    void deleteAll();
}
