package ru.practicum.shareit.item.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.common.AbstractMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class ItemMapper extends AbstractMapper {
    public ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.isAvailable())
                .build();
    }

    public ItemWithAdditionalDataDto toItemWithAdditionalDataDto(Item item) {
        return ItemWithAdditionalDataDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.isAvailable())
                .build();
    }

    public Item toItem(ItemDto itemDto, User owner) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .isAvailable(itemDto.getIsAvailable())
                .owner(owner)
                .build();
    }

    public Item toItem(ItemCreateDto itemDto, User owner) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .isAvailable(itemDto.getIsAvailable())
                .owner(owner)
                .build();
    }

    public Item updateIfDifferent(final Item item, final ItemDto itemWithChanges) {
        return Item.builder()
                .id(item.getId())
                .owner(item.getOwner())
                .name(getChanged(item.getName(), itemWithChanges.getName()))
                .description(getChanged(item.getDescription(), itemWithChanges.getDescription()))
                .isAvailable(getChanged(item.isAvailable(), itemWithChanges.getIsAvailable()))
                .build();
    }
}
