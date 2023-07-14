package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.NotOwnerAccessException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toUnmodifiableList;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Long create(ItemCreateDto item, long ownerId) {
        final Optional<User> ownerOpt = userRepository.findById(ownerId);

        if (ownerOpt.isEmpty()) {
            throw new UserNotFoundException(ownerId);
        }

        final User owner = ownerOpt.get();
        Item itemEntity = ItemMapper.toItem(item, owner);
        itemEntity = itemRepository.save(itemEntity);

        return itemEntity.getId();
    }

    @Override
    public ItemDto createAndGet(ItemCreateDto item, long ownerId) {
        final Long itemId = create(item, ownerId);
        return getOwnerItemById(itemId, ownerId);
    }

    @Override
    public ItemDto update(long id, ItemDto item, long ownerId) {
        checkItemExists(id);
        checkUserExists(ownerId);
        checkUserOwnItem(ownerId, id);

        // Обновление
        final Optional<Item> itemOpt = itemRepository.findByIdAndOwnerId(id, ownerId);
        final Item itemFromRepo = itemOpt.orElseThrow(() -> new ItemNotFoundException(id));

        final Item changedItem = ItemMapper.updateIfDifferent(itemFromRepo, item);

        Item updatedItem;
        if (itemFromRepo.equals(changedItem)) {
            updatedItem = changedItem;
        } else {
            updatedItem = itemRepository.save(changedItem);
        }

        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getById(long id) {
        final Optional<Item> itemOpt = itemRepository.findById(id);
        final Item item = itemOpt.orElseThrow(() -> new ItemNotFoundException(id));

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getOwnerItemById(long itemId, long ownerId) {
        checkItemExists(itemId);
        checkUserExists(ownerId);

        final Optional<Item> itemOpt = itemRepository.findByIdAndOwnerId(itemId, ownerId);
        final Item item = itemOpt.orElseThrow(() -> new ItemNotFoundException(itemId));

        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllOwnerItems(long ownerId) {
        checkUserExists(ownerId);

        final List<Item> ownerItems = itemRepository.findByOwnerId(ownerId);
        return ownerItems.stream().map(ItemMapper::toItemDto).collect(toUnmodifiableList());
    }

    @Override
    public List<ItemDto> searchItems(String text, long userId) {
        final String searchText = text.trim().toLowerCase();

        List<Item> searchResult = new ArrayList<>();
        if (!searchText.isEmpty()) {
           // searchResult = itemRepository.search(searchText, userId);
            searchResult = itemRepository.findAvailableByNameOrDescription(searchText); // AndIsAvailableIsTrue
        }

        return searchResult.stream().map(ItemMapper::toItemDto).collect(toUnmodifiableList());
    }

    private void checkUserOwnItem(long userId, long itemId) {
        if (!itemRepository.existsByIdAndOwnerId(userId, itemId)) {
            throw new NotOwnerAccessException(String.format("Вещь с id = %s не принадлежит пользователю с id = %s", itemId, userId));
        }
    }

    private void checkUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }

    private void checkItemExists(long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new ItemNotFoundException(itemId);
        }
    }
}
