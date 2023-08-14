package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemServiceIT {
    private final ItemService itemService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    private User owner;
    private User user;
    private User booker;
    private Item item;

    @BeforeEach
    public void init() {
        owner = User.builder()
                .name("owner")
                .email("owner@email.com")
                .build();
        owner = userRepository.save(owner);

        user = User.builder()
                .name("user")
                .email("user@email.com")
                .build();
        user = userRepository.save(user);

        booker = User.builder()
                .name("booker")
                .email("booker@email.com")
                .build();
        booker = userRepository.save(booker);

        item = Item.builder()
                .name("Item")
                .description("item desc")
                .owner(owner)
                .isAvailable(true)
                .build();
        item = itemRepository.save(item);
    }

    @Test
    public void getById_whenOk_thenReturnItem() {
        val userId = user.getId();
        val itemId = item.getId();

        val itemFromService = itemService.getById(itemId, userId);

        assertEquals(item.getId(), itemFromService.getId());
        assertEquals(item.getName(), itemFromService.getName());
        assertEquals(item.getDescription(), itemFromService.getDescription());
        assertEquals(item.isAvailable(), itemFromService.getIsAvailable());
        assertTrue(itemFromService.getComments().isEmpty());
        assertNull(itemFromService.getLastBooking());
        assertNull(itemFromService.getNextBooking());
    }

    @Test
    public void getById_whenItemNotExisted_thenThrowException() {
        val userId = user.getId();
        val notExistedItemId = 9999L;

        assertFalse(itemRepository.existsById(notExistedItemId));
        assertThrows(ItemNotFoundException.class, () -> itemService.getById(notExistedItemId, userId));
    }

    @Test
    public void getById_whenHasComments_thenReturnItem() {
        val userId = user.getId();
        val itemId = item.getId();

        var itemFromService = itemService.getById(itemId, userId);
        assertTrue(itemFromService.getComments().isEmpty());

        val booking = Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().withNano(0).minusDays(3))
                .end(LocalDateTime.now().withNano(0).plusDays(1))
                .build();
        bookingRepository.save(booking);

        val commentCount = 3;
        val itemComments = new ArrayList<>();
        for (int counter = 1; counter <= commentCount; counter++) {
            val commentCreateDto = CommentCreateDto.builder()
                    .text("comment " + counter)
                    .build();
            val comment = itemService.addComment(itemId, booker.getId(), commentCreateDto);

            itemComments.add(comment);
        }

        itemFromService = itemService.getById(itemId, userId);
        val commentsToItem = itemFromService.getComments();
        assertFalse(commentsToItem.isEmpty());
        assertEquals(itemComments.size(), commentsToItem.size());
        assertEquals(itemComments, commentsToItem);
    }

    @Test
    public void getById_whenHasBookingButNotOwner_thenReturnItem() {
        var lastBooking = Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().withNano(0).minusDays(3))
                .end(LocalDateTime.now().withNano(0).plusDays(1))
                .build();
        lastBooking = bookingRepository.save(lastBooking);

        var nextBooking = Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().withNano(0).plusDays(3))
                .end(LocalDateTime.now().withNano(0).plusDays(1))
                .build();
        nextBooking = bookingRepository.save(nextBooking);

        val userId = user.getId();
        val itemId = item.getId();
        val itemFromService = itemService.getById(itemId, userId);

        assertEquals(itemId, itemFromService.getId());
        assertNull(itemFromService.getLastBooking());
        assertNull(itemFromService.getNextBooking());
        assertNotNull(lastBooking);
        assertNotNull(nextBooking);
        assertEquals(itemId, lastBooking.getItem().getId());
        assertEquals(itemId, nextBooking.getItem().getId());
    }

    @Test
    public void getById_whenHasBookingAndOwner_thenReturnItem() {
        var lastBooking = Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().withNano(0).minusDays(3))
                .end(LocalDateTime.now().withNano(0).plusDays(1))
                .build();
        lastBooking = bookingRepository.save(lastBooking);

        var nextBooking = Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().withNano(0).plusDays(3))
                .end(LocalDateTime.now().withNano(0).plusDays(1))
                .build();
        nextBooking = bookingRepository.save(nextBooking);

        val ownerId = owner.getId();
        val itemId = item.getId();
        val itemFromService = itemService.getById(itemId, ownerId);

        assertEquals(itemId, itemFromService.getId());
        assertNotNull(itemFromService.getLastBooking());
        assertNotNull(itemFromService.getNextBooking());
        assertNotNull(lastBooking);
        assertNotNull(nextBooking);
        assertEquals(itemId, lastBooking.getItem().getId());
        assertEquals(itemId, nextBooking.getItem().getId());
        assertEquals(BookingMapper.toBookingForItemDto(lastBooking), itemFromService.getLastBooking());
        assertEquals(BookingMapper.toBookingForItemDto(nextBooking), itemFromService.getNextBooking());
    }

    @Test
    public void search_whenNoItems_thenReturnEmpty() {
        itemRepository.deleteAll();
        assertEquals(0, itemRepository.count());

        val searchingItems = itemService.searchItems("any item", user.getId());
        assertTrue(searchingItems.isEmpty());
    }

    @Test
    public void search_whenHasItemsAndSearchOk_thenReturnItems() {
        val newItemCreateDto = ItemCreateDto.builder()
                .name("Java for beginners")
                .description("book")
                .isAvailable(true)
                .build();
        val item = itemService.createAndGet(newItemCreateDto, owner.getId());

        var searchingItems = itemService.searchItems("book", user.getId());
        assertFalse(searchingItems.isEmpty());
        assertEquals(item, searchingItems.get(0));

        searchingItems = itemService.searchItems("JAVA", user.getId());
        assertFalse(searchingItems.isEmpty());
        assertEquals(item, searchingItems.get(0));

        searchingItems = itemService.searchItems("BeGiN", user.getId());
        assertFalse(searchingItems.isEmpty());
        assertEquals(item, searchingItems.get(0));
    }

    @Test
    public void search_whenSearchBlank_thenReturnEmpty() {
        val newItemCreateDto = ItemCreateDto.builder()
                .name("Java for beginners")
                .description("book")
                .isAvailable(true)
                .build();
        val item = itemService.createAndGet(newItemCreateDto, owner.getId());

        assertNotEquals(0, itemRepository.count());

        val searchingItems = itemService.searchItems("", user.getId());
        assertTrue(searchingItems.isEmpty());
    }

    @Test
    public void search_whenHasItemAndSearchNotOk_thenReturnItems() {
        val newItemCreateDto = ItemCreateDto.builder()
                .name("Java for beginners")
                .description("book")
                .isAvailable(true)
                .build();
        val item = itemService.createAndGet(newItemCreateDto, owner.getId());

        val searchingItems = itemService.searchItems("HAMMER", user.getId());
        assertTrue(searchingItems.isEmpty());
    }

    @Test
    public void search_whenHasPaginationAllItems_thenReturnItems() {
        val itemsList = new ArrayList<>();
        val itemsCount = 3;
        for (int counter = 1; counter <= itemsCount; counter++) {
            val newItemCreateDto = ItemCreateDto.builder()
                    .name("Harry Potter")
                    .description("Tom " + counter)
                    .isAvailable(true)
                    .build();
            val item = itemService.createAndGet(newItemCreateDto, owner.getId());
            itemsList.add(item);
        }

        val from = 0;
        val size = itemsCount;
        val searchingItems = itemService.searchItems("Potter", user.getId(), from, size);
        assertEquals(size, searchingItems.size());
        assertEquals(itemsList, searchingItems);
    }

    @Test
    public void search_whenHasPagination_Items4From0Size1_thenReturnItems() {
        val itemsList = new ArrayList<>();
        val itemsCount = 3;
        for (int counter = 1; counter <= itemsCount; counter++) {
            val newItemCreateDto = ItemCreateDto.builder()
                    .name("Harry Potter")
                    .description("Tom " + counter)
                    .isAvailable(true)
                    .build();
            val item = itemService.createAndGet(newItemCreateDto, owner.getId());
            itemsList.add(item);
        }

        val from = 0;
        val size = 1;
        val searchingItems = itemService.searchItems("Potter", user.getId(), from, size);
        assertEquals(size, searchingItems.size());
        assertThat(itemsList).containsAnyElementsOf(searchingItems);
    }

    @Test
    public void search_whenHasPagination_Items7From1Size5_thenReturnItems() {
        val itemsList = new ArrayList<>();
        val itemsCount = 7;
        for (int counter = 1; counter <= itemsCount; counter++) {
            val newItemCreateDto = ItemCreateDto.builder()
                    .name("Harry Potter")
                    .description("Tom " + counter)
                    .isAvailable(true)
                    .build();
            val item = itemService.createAndGet(newItemCreateDto, owner.getId());
            itemsList.add(item);
        }

        val from = 1;
        val size = 5;
        val searchingItems = itemService.searchItems("Potter", user.getId(), from, size);
        assertEquals(size, searchingItems.size());
        assertThat(itemsList).containsAnyElementsOf(searchingItems);
    }

    @Test
    public void search_whenHasPagination_Items7From5Size10_thenReturnItems() {
        val itemsList = new ArrayList<>();
        val itemsCount = 7;
        for (int counter = 1; counter <= itemsCount; counter++) {
            val newItemCreateDto = ItemCreateDto.builder()
                    .name("Harry Potter")
                    .description("Tom " + counter)
                    .isAvailable(true)
                    .build();
            val item = itemService.createAndGet(newItemCreateDto, owner.getId());
            itemsList.add(item);
        }

        val from = 5;
        val size = 10;
        val searchingItems = itemService.searchItems("Potter", user.getId(), from, size);
        assertEquals(itemsCount - from, searchingItems.size());
        assertThat(itemsList).containsAnyElementsOf(searchingItems);
    }

    @AfterEach
    public void clean() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }
}
