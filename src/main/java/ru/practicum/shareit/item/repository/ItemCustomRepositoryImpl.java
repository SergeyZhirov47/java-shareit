package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.Optional;

@RequiredArgsConstructor
public class ItemCustomRepositoryImpl implements ItemCustomRepository {
    private final ItemRepository itemRepository;

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
