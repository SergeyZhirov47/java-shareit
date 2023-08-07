package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DAOItemImpl implements DAOItem {
    private final ItemRepository itemRepository;

    @Override
    public Optional<Item> findByIdAndOwnerId(long id, long ownerId) {
        return itemRepository.findByIdAndOwnerId(id, ownerId);
    }

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
    public Item getItemById(long id) {
        final Optional<Item> itemOpt = itemRepository.findById(id);
        return itemOpt.orElseThrow(() -> new ItemNotFoundException(id));
    }

    @Override
    public void checkItemExists(long id) {
        if (!itemRepository.existsById(id)) {
            throw new ItemNotFoundException(id);
        }
    }

    @Override
    public Item getByIdAndOwnerId(long id, long ownerId) {
        final Optional<Item> itemOpt = itemRepository.findByIdAndOwnerId(id, ownerId);
        return itemOpt.orElseThrow(() -> new ItemNotFoundException(id));
    }
}
