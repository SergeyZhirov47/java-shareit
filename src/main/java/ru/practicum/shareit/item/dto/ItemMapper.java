package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static java.util.Objects.isNull;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.isAvailable())
                .build();
    }

    public static Item toItem(ItemDto itemDto, User owner) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .isAvailable(itemDto.getIsAvailable())
                .owner(owner)
                .build();
    }

    public static Item toItem(ItemCreateDto itemDto, User owner) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .isAvailable(itemDto.getIsAvailable())
                .owner(owner)
                .build();
    }

    public static Item updateIfDifferent(final Item item, final ItemDto itemWithChanges) {
        return Item.builder()
                .id(item.getId())
                .owner(item.getOwner())
                .name(getChanged(item.getName(), itemWithChanges.getName()))
                .description(getChanged(item.getDescription(), itemWithChanges.getDescription()))
                .isAvailable(getChanged(item.isAvailable(), itemWithChanges.getIsAvailable()))
                .build();
    }

    // ToDo
    // Вынести эти два метода в какой-то класс прородитель?
    public static <T> T getChanged(T original, T changed, boolean changedNullable) {
        if (isNull(changed) && !changedNullable) {
            return original;
        }

        if (isNull(original) || !original.equals(changed)) {
            return changed;
        }

        return original;
    }

    public static <T> T getChanged(T original, T changed) {
        return getChanged(original, changed, false);
    }
}
