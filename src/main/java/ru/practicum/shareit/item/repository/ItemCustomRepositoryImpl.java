package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.Optional;

@RequiredArgsConstructor
public class ItemCustomRepositoryImpl implements ItemCustomRepository {
    private final JPAItemRepository jpaItemRepository;

    @Override
    public Item getItemById(long id) {
        final Optional<Item> itemOpt = jpaItemRepository.findById(id);
        return itemOpt.orElseThrow(() -> new ItemNotFoundException(id));
    }

    @Override
    public void checkItemExists(long id) {
        if (!jpaItemRepository.existsById(id)) {
            throw new ItemNotFoundException(id);
        }
    }

    @Override
    public Item getByIdAndOwnerId(long id, long ownerId) {
        final Optional<Item> itemOpt = jpaItemRepository.findByIdAndOwnerId(id, ownerId);
        return itemOpt.orElseThrow(() -> new ItemNotFoundException(id));
    }
}
