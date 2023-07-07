package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.NotFoundException;
import ru.practicum.shareit.item.exception.NotOwnerAccessException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
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
            throw new NotFoundException(String.format("Пользователь с id = %s не найден", ownerId));
        }

        final User owner = ownerOpt.get();
        final Item itemEntity = ItemMapper.toItem(item, owner);
        return itemRepository.add(itemEntity, owner.getId());
    }

    @Override
    public ItemDto createAndGet(ItemCreateDto item, long ownerId) {
        final Long itemId = create(item, ownerId);
        return getOwnerItemById(itemId, ownerId);
    }

    @Override
    public ItemDto update(long id, ItemDto item, long ownerId) {
        // ToDo
        // Проверка есть ли вещь по id
        // Проверка есть ли пользователь по id
        checkUserOwnItem(ownerId, id);

        // Обновление
        final Optional<Item> itemOpt = itemRepository.getByIdAndOwnerId(id, ownerId);
        final Item itemFromRepo = unpackItem(itemOpt, id);

        final Item changedItem = ItemMapper.updateIfDifferent(itemFromRepo, item);

        Item updatedItem;
        if (itemFromRepo.equals(changedItem)) {
            updatedItem = changedItem;
        } else {
            updatedItem = itemRepository.update(changedItem, ownerId);
        }

        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getById(long id) {
        final Optional<Item> itemOpt = itemRepository.getById(id);
        final Item item = unpackItem(itemOpt, id);

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getOwnerItemById(long itemId, long ownerId) {
        // ToDo
        // Проверка есть ли вещь по id
        // Проверка есть ли пользователь по id
        // ToDo 2
        // Как можно объединять проверки?
        // По идее 3 проверки - 3 потенциальных запроса в БД.
        // Ну или как минимум не удобно. Хотелось бы в проверке принадлежит ли вещь владельцу проверять и наличие вещи и владельца.

        final Optional<Item> itemOpt = itemRepository.getByIdAndOwnerId(itemId, ownerId);
        final Item item = unpackItem(itemOpt, itemId);

        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllOwnerItems(long ownerId) {
        // ToDo
        // Проверка, что пользователь есть?
        final List<Item> ownerItems = itemRepository.getOwnerItems(ownerId);
        return ownerItems.stream().map(ItemMapper::toItemDto).collect(toUnmodifiableList());
    }

    @Override
    public List<ItemDto> searchItems(String text, long userId) {
        final String searchText = text.trim().toLowerCase();

        List<Item> searchResult = new ArrayList<>();
        if (!searchText.isEmpty()) {
            searchResult = itemRepository.search(searchText, userId);
        }

        return searchResult.stream().map(ItemMapper::toItemDto).collect(toUnmodifiableList());
    }

    /*
    private Item getItemById(Long id) {
        final Optional<Item> itemOpt = itemRepository.getById(id);

        if (itemOpt.isEmpty()) {
            throw new NotFoundException(String.format("Предмет с id = %s не найден", id));
        }

        return itemOpt.get();
    }
     */

    private Item unpackItem(Optional<Item> itemOpt, long id) {
        if (itemOpt.isEmpty()) {
            throw new NotFoundException(String.format("Вещь с id = %s не найдена", id));
        }

        return itemOpt.get();
    }

    private void checkUserOwnItem(long userId, long itemId) {
        if (!itemRepository.checkUserOwnItem(userId, itemId)) {
            throw new NotOwnerAccessException(String.format("Вещь с id = %s не принадлежит пользователю с id = %s", itemId, userId));
        }
    }
}
