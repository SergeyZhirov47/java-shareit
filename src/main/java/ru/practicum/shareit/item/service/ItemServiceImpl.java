package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.NotOwnerAccessException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toUnmodifiableList;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

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
    public ItemWithBookingDto getById(long id, long userId) {
        final Optional<Item> itemOpt = itemRepository.findById(id);
        final Item item = itemOpt.orElseThrow(() -> new ItemNotFoundException(id));

        final ItemWithBookingDto itemWithBookingDto = ItemMapper.toItemWithBookingDto(item);

        // Данные о бронировании может видеть только владелец вещи.
        if (item.getOwner().getId().equals(userId)) {
            final LocalDateTime now = LocalDateTime.now();

            final Booking lastBooking = bookingRepository.getLastBookingForItemById(id, now);
            final Booking nextBooking = bookingRepository.getNextBookingForItemById(id, now);
            setLastAndNextBooking(itemWithBookingDto, lastBooking, nextBooking);
        }

        return itemWithBookingDto;
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
    public List<ItemWithBookingDto> getAllOwnerItems(long ownerId) {
        checkUserExists(ownerId);

        final List<Item> ownerItems = itemRepository.findByOwnerId(ownerId);
        final List<ItemWithBookingDto> ownerItemDto = new ArrayList<>();

        if (!ownerItems.isEmpty()) {
            final List<Long> itemIds = ownerItems.stream().map(Item::getId).collect(toUnmodifiableList());

            // Получение последнего и последующего бронирования для каждого предмета.
            final LocalDateTime now = LocalDateTime.now();
            final Map<Long, Booking> lastBookings = bookingRepository.getLastBookingForItemsByIdList(itemIds, now);
            final Map<Long, Booking> nextBookings = bookingRepository.getNextBookingForItemsByIdList(itemIds, now);

            // Установка последнего и последующего бронирования для каждого предмета.
            for (final Item item : ownerItems) {
                final ItemWithBookingDto itemWithBookingDto = ItemMapper.toItemWithBookingDto(item);

                final Booking lastBooking = lastBookings.get(item.getId());
                final Booking nextBooking = nextBookings.get(item.getId());
                setLastAndNextBooking(itemWithBookingDto, lastBooking, nextBooking);

                ownerItemDto.add(itemWithBookingDto);
            }
        }

        return ownerItemDto;
    }

    @Override
    public List<ItemDto> searchItems(String text, long userId) {
        final String searchText = text.trim().toLowerCase();

        List<Item> searchResult = new ArrayList<>();
        if (!searchText.isEmpty()) {
            searchResult = itemRepository.findAvailableByNameOrDescription(searchText);
        }

        return searchResult.stream().map(ItemMapper::toItemDto).collect(toUnmodifiableList());
    }

    private void checkUserOwnItem(long userId, long itemId) {
        if (!itemRepository.existsByIdAndOwnerId(itemId, userId)) {
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

    private void setLastAndNextBooking(ItemWithBookingDto itemWithBookingDto, Booking lastBooking, Booking nextBooking) {
        itemWithBookingDto.setLastBooking(BookingMapper.toBookingForItemDto(lastBooking));
        itemWithBookingDto.setNextBooking(BookingMapper.toBookingForItemDto(nextBooking));
    }
}
