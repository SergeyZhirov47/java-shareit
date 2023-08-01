package ru.practicum.shareit.item.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.common.AbstractMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static java.util.Objects.isNull;

@UtilityClass
public class ItemMapper extends AbstractMapper {
    public ItemDto toItemDto(Item item) {
        if (isNull(item)) return null;

        final Long requestId = isNull(item.getRequest()) ? null : item.getRequest().getId();

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.isAvailable())
                .requestId(requestId)
                .build();
    }

    public ItemWithAdditionalDataDto toItemWithAdditionalDataDto(Item item) {
        if (isNull(item)) return null;

        return ItemWithAdditionalDataDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.isAvailable())
                .build();
    }

    public Item toItem(ItemDto itemDto, User owner) {
        if (isNull(itemDto)) return null;

        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .isAvailable(itemDto.getIsAvailable())
                .owner(owner)
                .build();
    }

    public Item toItem(ItemCreateDto itemDto, User owner) {
        if (isNull(itemDto)) return null;

        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .isAvailable(itemDto.getIsAvailable())
                .owner(owner)
                .build();
    }

    public Item toItem(ItemCreateDto itemDto) {
        if (isNull(itemDto)) return null;

        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .isAvailable(itemDto.getIsAvailable())
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
