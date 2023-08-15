package ru.practicum.shareit.request.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toUnmodifiableList;

@UtilityClass
public class ItemRequestMapper {
    public ItemRequest toItemRequest(ItemRequestCreateDto itemRequestCreateDto) {
        if (isNull(itemRequestCreateDto)) return null;

        return ItemRequest.builder()
                .description(itemRequestCreateDto.getDescription())
                .build();
    }

    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        if (isNull(itemRequest)) return null;

        final UserDto requestor = UserMapper.toUserDto(itemRequest.getRequestor());
        final List<ItemDto> items = Stream.ofNullable(itemRequest.getItemsByRequest())
                .flatMap(Collection::stream)
                .map(ItemMapper::toItemDto)
                .collect(toUnmodifiableList());

        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .requestor(requestor)
                .itemsByRequest(items)
                .build();
    }

    public List<ItemRequestDto> toItemRequestDtoList(List<ItemRequest> itemRequests) {
        return Stream.ofNullable(itemRequests)
                .flatMap(Collection::stream)
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(toUnmodifiableList());
    }
}
