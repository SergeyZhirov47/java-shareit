package ru.practicum.shareit.item.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.common.IdGenerator;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    // Все вещи.
    // Ключ - id вещи.
    // Значение - вещь.
    private final Map<Long, Item> allItems = new HashMap<>();
    // Вещи по владельцу.
    // Ключ - id владельца.
    // Значение - мапа его вещей.
    private final Map<Long, Map<Long, Item>> itemsByOwner = new HashMap<>();
    private final IdGenerator idGenerator = new IdGenerator(0L);

    @Override
    public Optional<Item> getById(long id) {
        return Optional.ofNullable(allItems.get(id));
    }

    @Override
    public Optional<Item> getByIdAndOwnerId(long id, long ownerId) {
        if (!itemsByOwner.containsKey(ownerId)) {
            return Optional.empty();
        }

        return Optional.ofNullable(itemsByOwner.get(ownerId).get(id));
    }

    @Override
    public List<Item> getOwnerItems(long ownerId) {
        List<Item> ownerItems = new ArrayList<>();

        if (itemsByOwner.containsKey(ownerId)) {
            ownerItems = itemsByOwner.get(ownerId).values().stream().collect(Collectors.toUnmodifiableList());
        }

        return ownerItems;
    }

    @Override
    public Long add(Item item, long ownerId) {
        final Long itemId = idGenerator.getNext();
        item.setId(itemId);

        final Map<Long, Item> ownerItems = itemsByOwner.getOrDefault(ownerId, new HashMap<>());
        ownerItems.put(itemId, item);
        itemsByOwner.put(ownerId, ownerItems);
        allItems.put(itemId, item);

        return itemId;
    }

    @Override
    public Item update(Item item, long ownerId) {
        if (itemsByOwner.containsKey(ownerId)) {
            final Long itemId = item.getId();

            itemsByOwner.get(ownerId).put(itemId, item);
            allItems.put(itemId, item);
        }

        return item;
    }

    @Override
    public List<Item> search(String text, long userId) {
        final Predicate<Item> containsInNameOrDescriptionPredicate = (item -> item.getName().toLowerCase().contains(text)
                || item.getDescription().toLowerCase().contains(text));

        final List<Item> searchResult = allItems.values().stream()
                .filter(Item::isAvailable)
                .filter(containsInNameOrDescriptionPredicate)
                .collect(Collectors.toUnmodifiableList());

        return searchResult;
    }

    @Override
    public boolean contains(long id) {
        return allItems.containsKey(id);
    }

    @Override
    public boolean checkUserOwnItem(long userId, long itemId) {
        return itemsByOwner.containsKey(userId) && itemsByOwner.get(userId).containsKey(itemId);
    }
}
