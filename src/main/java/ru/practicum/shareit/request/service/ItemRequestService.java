package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    Long create(ItemRequestCreateDto itemRequest, long userId);

    ItemRequestDto createAndGet(ItemRequestCreateDto itemRequest, long userId);

    List<ItemRequestDto> getAllUserItemRequests(long userId);

    List<ItemRequestDto> getAllItemRequests(long userId, Integer from, Integer size);

    ItemRequestDto getItemRequestById(long id);
}
