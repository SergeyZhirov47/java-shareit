package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

@Repository
public interface DAOItem {
    Optional<Item> findByIdAndOwnerId(long id, long ownerId);

    List<Item> findByOwnerId(long ownerId);

    List<Item> findByOwnerId(long ownerId, Pageable pageable);

    boolean existsByIdAndOwnerId(long itemId, long userId);

    List<Item> findAvailableByNameOrDescription(String text);

    List<Item> findAvailableByNameOrDescription(String text, Pageable pageable);

    Item getItemById(long id);

    void checkItemExists(long id);

    Item getByIdAndOwnerId(long id, long ownerId);

    Item save(Item entity);
}
