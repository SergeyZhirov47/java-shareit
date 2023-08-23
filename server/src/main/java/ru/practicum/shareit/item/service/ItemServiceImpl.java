package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.exception.NotOwnerAccessException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.DaoItem;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.DaoUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.*;
import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static ru.practicum.shareit.common.Utils.createOffsetBasedPageRequest;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final DaoItem daoItem;
    private final DaoUser daoUser;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Transactional
    @Override
    public Long create(ItemCreateDto item, long ownerId) {
        final User owner = daoUser.getUserById(ownerId);

        // ToDo
        // 1. Получение заявки на вещь и проброс исключения вынести в репозиторий.
        ItemRequest itemRequest = null;
        final Long requestId = item.getRequestId();
        if (nonNull(requestId)) {
            itemRequest = itemRequestRepository.findById(requestId)
                    .orElseThrow(() -> new ItemRequestNotFoundException(requestId));
        }

        Item itemEntity = ItemMapper.toItem(item);
        itemEntity.setOwner(owner);
        itemEntity.setRequest(itemRequest);
        itemEntity = daoItem.save(itemEntity);

        return itemEntity.getId();
    }

    @Transactional
    @Override
    public ItemDto createAndGet(ItemCreateDto item, long ownerId) {
        final Long itemId = create(item, ownerId);
        return getOwnerItemById(itemId, ownerId);
    }

    @Transactional
    @Override
    public ItemDto update(long id, ItemDto item, long ownerId) {
        daoItem.checkItemExists(id);
        daoUser.checkUserExists(ownerId);
        checkUserOwnItem(ownerId, id);

        // Обновление
        final Item itemFromRepo = daoItem.getByIdAndOwnerId(id, ownerId);
        final Item changedItem = ItemMapper.updateIfDifferent(itemFromRepo, item);
        final Item updatedItem = daoItem.save(changedItem);

        return ItemMapper.toItemDto(updatedItem);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemWithAdditionalDataDto getById(long id, long userId) {
        final Item item = daoItem.getItemById(id);
        final ItemWithAdditionalDataDto itemWithAdditionalDataDto = ItemMapper.toItemWithAdditionalDataDto(item);

        // Комментарии.
        final List<Comment> commentsList = commentRepository.findByItemId(id);
        final List<CommentDto> commentDtoList = commentsList.stream().map(CommentMapper::toCommentDto).collect(toUnmodifiableList());
        itemWithAdditionalDataDto.setComments(commentDtoList);

        // Данные о бронировании может видеть только владелец вещи.
        if (item.getOwner().getId().equals(userId)) {
            final LocalDateTime now = LocalDateTime.now();

            final Booking lastBooking = bookingRepository.getLastBookingForItemById(id, now);
            final Booking nextBooking = bookingRepository.getNextBookingForItemById(id, now);
            setLastAndNextBooking(itemWithAdditionalDataDto, lastBooking, nextBooking);
        }

        return itemWithAdditionalDataDto;
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDto getOwnerItemById(long itemId, long ownerId) {
        daoItem.checkItemExists(itemId);
        daoUser.checkUserExists(ownerId);

        final Item item = daoItem.getByIdAndOwnerId(itemId, ownerId);
        return ItemMapper.toItemDto(item);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemWithAdditionalDataDto> getAllOwnerItems(long ownerId) {
        return getAllOwnerItems(ownerId, null);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemWithAdditionalDataDto> getAllOwnerItems(long ownerId, Integer from, Integer size) {
        return getAllOwnerItems(ownerId, createOffsetBasedPageRequest(from, size, Sort.by("id")));
    }

    @Transactional(readOnly = true)
    private List<ItemWithAdditionalDataDto> getAllOwnerItems(long ownerId, Pageable pageable) {
        daoUser.checkUserExists(ownerId);

        final List<Item> ownerItems = daoItem.findByOwnerId(ownerId, pageable);
        final List<ItemWithAdditionalDataDto> ownerItemDtoList = new ArrayList<>();

        if (ownerItems.isEmpty()) {
            return ownerItemDtoList;
        }

        final List<Long> itemIds = ownerItems.stream().map(Item::getId).collect(toUnmodifiableList());

        // Получение последнего и последующего бронирования для каждого предмета.
        final LocalDateTime now = LocalDateTime.now();
        final Map<Long, Booking> lastBookings = bookingRepository.getLastBookingForItemsByIdList(itemIds, now);
        final Map<Long, Booking> nextBookings = bookingRepository.getNextBookingForItemsByIdList(itemIds, now);

        // Получить комментарии для каждой вещи.
        final Map<Long, List<Comment>> comments = commentRepository.findByItemIdIn(itemIds, Sort.by(DESC, "created"))
                .stream()
                .collect(groupingBy(c -> c.getItem().getId(), toList()));

        // Установка последнего и последующего бронирования для каждого предмета, а также комментариев.
        for (final Item item : ownerItems) {
            final long itemId = item.getId();
            final ItemWithAdditionalDataDto itemWithAdditionalDataDto = ItemMapper.toItemWithAdditionalDataDto(item);

            final Booking lastBooking = lastBookings.get(itemId);
            final Booking nextBooking = nextBookings.get(itemId);
            setLastAndNextBooking(itemWithAdditionalDataDto, lastBooking, nextBooking);

            final List<Comment> commentsToItem = comments.get(itemId);
            if (nonNull(commentsToItem)) {
                final List<CommentDto> commentDtoList = commentsToItem.stream().map(CommentMapper::toCommentDto).collect(toUnmodifiableList());
                itemWithAdditionalDataDto.setComments(commentDtoList);
            }

            ownerItemDtoList.add(itemWithAdditionalDataDto);
        }

        return ownerItemDtoList;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> searchItems(String text, long userId) {
        return searchItems(text, userId, null);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> searchItems(String text, long userId, Integer from, Integer size) {
        return searchItems(text, userId, createOffsetBasedPageRequest(from, size));
    }

    @Transactional(readOnly = true)
    private List<ItemDto> searchItems(String text, long userId, Pageable pageable) {
        daoUser.checkUserExists(userId);

        if (isBlank(text)) {
            return Collections.emptyList();
        }

        final String searchText = text.trim().toLowerCase();
        return daoItem.findAvailableByNameOrDescription(searchText, pageable)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(toUnmodifiableList());
    }

    @Transactional
    @Override
    public CommentDto addComment(long itemId, long userId, CommentCreateDto commentDto) {
        final Item item = daoItem.getItemById(itemId); // Проверяем (и получаем) существует ли вещь.
        final User user = daoUser.getUserById(userId); // Проверяем (и получаем) существует ли пользователь.

        // Проверяем, что пользователь брал в аренду вещь.
        final boolean isUserBookingItem = bookingRepository.isUserBookingItem(userId, itemId, LocalDateTime.now());

        if (!isUserBookingItem) {
            throw new UnsupportedOperationException(String.format("Нельзя оставлять комментарий к вещи, которой еще не пользовался (id пользователя = %s, id вещи = %s)", userId, itemId));
        }

        Comment comment = CommentMapper.toComment(commentDto);
        comment.setAuthor(user);
        comment.setItem(item);

        comment = commentRepository.save(comment);

        return CommentMapper.toCommentDto(comment);
    }

    private void checkUserOwnItem(long userId, long itemId) {
        if (!daoItem.existsByIdAndOwnerId(itemId, userId)) {
            throw new NotOwnerAccessException(String.format("Вещь с id = %s не принадлежит пользователю с id = %s", itemId, userId));
        }
    }

    private void setLastAndNextBooking(ItemWithAdditionalDataDto itemWithAdditionalDataDto, Booking lastBooking, Booking nextBooking) {
        itemWithAdditionalDataDto.setLastBooking(BookingMapper.toBookingForItemDto(lastBooking));
        itemWithAdditionalDataDto.setNextBooking(BookingMapper.toBookingForItemDto(nextBooking));
    }
}
