package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    private final Sort itemRequestCreatedSort = Sort.by("created");

    @Transactional
    @Override
    public Long create(ItemRequestCreateDto itemRequest, long userId) {
        final ItemRequest newItemRequest = createModel(itemRequest, userId);
        return newItemRequest.getId();
    }

    @Transactional
    @Override
    public ItemRequestDto createAndGet(ItemRequestCreateDto itemRequest, long userId) {
        final ItemRequest newItemRequest = createModel(itemRequest, userId);
        return ItemRequestMapper.toItemRequestDto(newItemRequest);
    }

    // Получить список своих запросов вместе с данными об ответах на них.
    // Запросы должны возвращаться в отсортированном порядке от более новых к более старым.
    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestDto> getAllUserItemRequests(long userId) {
        userRepository.checkUserExists(userId);

        final List<ItemRequest> itemRequests = itemRequestRepository.findByRequestorId(userId, itemRequestCreatedSort.descending());
        return ItemRequestMapper.toItemRequestDtoList(itemRequests);
    }

    // Получить список запросов, созданных другими пользователями (свои запросы не нужны).
    // Запросы сортируются по дате создания: от более новых к более старым.
    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestDto> getAllItemRequests(long userId, Integer from, Integer size) {
        List<ItemRequest> itemRequests;

        if (nonNull(from) && nonNull(size)) {
            final Pageable pageAndSortedByCreated = PageRequest.of(from, size, itemRequestCreatedSort.descending());
            itemRequests = itemRequestRepository.findByRequestorIdNot(userId, pageAndSortedByCreated);
        } else {
            itemRequests = itemRequestRepository.findByRequestorIdNot(userId, itemRequestCreatedSort.descending());
        }

        return ItemRequestMapper.toItemRequestDtoList(itemRequests);
    }

    @Override
    public ItemRequestDto getItemRequestById(long id) {
        final ItemRequest itemRequest = getById(id);
        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }

    private ItemRequest getById(long id) {
       final Optional<ItemRequest> itemRequest =  itemRequestRepository.findById(id);
       return itemRequest.orElseThrow(() -> new ItemRequestNotFoundException(id));
    }

    private ItemRequest createModel(ItemRequestCreateDto itemRequest, long userId) {
        final User requestor = userRepository.getUserById(userId);
        ItemRequest itemRequestEntity = ItemRequestMapper.toItemRequest(itemRequest);
        itemRequestEntity.setRequestor(requestor);
        itemRequestEntity.setCreated(LocalDateTime.now());
        itemRequestEntity = itemRequestRepository.save(itemRequestEntity);

        return itemRequestEntity;
    }
}
