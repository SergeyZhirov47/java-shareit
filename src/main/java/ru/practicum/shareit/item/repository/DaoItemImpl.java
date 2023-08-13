package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Repository
@RequiredArgsConstructor
public class DaoItemImpl implements DaoItem {
    private final ItemRepository itemRepository;

    @Override
    public List<Item> findByOwnerId(long ownerId) {
        return itemRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Item> findByOwnerId(long ownerId, Pageable pageable) {
        return itemRepository.findByOwnerId(ownerId, pageable);
    }

    @Override
    public boolean existsByIdAndOwnerId(long itemId, long userId) {
        return itemRepository.existsByIdAndOwnerId(itemId, userId);
    }

    @Override
    public List<Item> findAvailableByNameOrDescription(String text) {
        return itemRepository.findAvailableByNameOrDescription(text);
    }

    @Override
    public List<Item> findAvailableByNameOrDescription(String text, Pageable pageable) {
        return itemRepository.findAvailableByNameOrDescription(text, pageable);
    }

    @Override
    public Item save(Item entity) {
        return itemRepository.save(entity);
    }

    @Override
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    @Override
    public void deleteAll() {
        itemRepository.deleteAll();
    }

    @Override
    public Item getItemById(long id) {
        final Optional<Item> itemOpt = itemRepository.findById(id);
        return itemOpt.orElseThrow(() -> new ItemNotFoundException(id));
    }

    @Override
    public boolean existsById(long id) {
        return itemRepository.existsById(id);
    }

    @Override
    public void checkItemExists(long id) {
        if (!itemRepository.existsById(id)) {
            throw new ItemNotFoundException(id);
        }
    }

    @Override
    public List<Item> findItemsForItemRequest(long requestId) {
        return itemRepository.findItemsForItemRequest(requestId);
    }

    @Override
    public Map<Long, List<Item>> findItemsForItemRequests(List<Long> requestIds) {
        if (isNull(requestIds) || requestIds.isEmpty()) {
            return Collections.emptyMap();
        }

        final List<Item> items = itemRepository.findItemsForItemRequests(requestIds);
        final Map<Long, List<Item>> result = new HashMap<>();
        for (final Item item : items) {
            final long requestId = item.getRequest().getId();

            final List<Item> itemsByRequest = result.get(requestId);
            if (nonNull(itemsByRequest)) {
                itemsByRequest.add(item);
            } else {
                result.put(requestId, new ArrayList<>(List.of(item)));
            }
        }

        return result;
    }

    @Override
    public Item getByIdAndOwnerId(long itemId, long ownerId) {
        final Optional<Item> itemOpt = itemRepository.findByIdAndOwnerId(itemId, ownerId);
        return itemOpt.orElseThrow(() -> new ItemNotFoundException(itemId));
    }
}
