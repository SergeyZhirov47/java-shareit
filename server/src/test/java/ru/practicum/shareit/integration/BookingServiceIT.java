package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStateForSearch;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingServiceIT {
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public User owner;
    public User booker;
    public Item item;

    @BeforeEach
    public void init() {
        owner = User.builder()
                .name("owner")
                .email("owner@email.com")
                .build();
        owner = userRepository.save(owner);

        item = Item.builder()
                .owner(owner)
                .name("item")
                .description("desc")
                .isAvailable(true)
                .build();
        item = itemRepository.save(item);

        booker = User.builder()
                .name("booker")
                .email("booker@email.com")
                .build();
        booker = userRepository.save(booker);
    }

    @Test
    public void create_whenOk_thenReturnBooking() {
        val newBookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        val newBooking = bookingService.create(newBookingCreateDto, booker.getId());
        assertNotNull(newBooking);
        assertEquals(BookingStatus.WAITING, newBooking.getStatus());
        assertEquals(booker.getId(), newBooking.getBooker().getId());
        assertEquals(item.getId(), newBooking.getItem().getId());
    }

    @Test
    public void create_whenItemNotAvailable_thenThrowException() {
        val newBookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        item.setAvailable(false);
        itemRepository.save(item);

        assertFalse(item.isAvailable());
        assertThrows(UnsupportedOperationException.class, () -> bookingService.create(newBookingCreateDto, booker.getId()));
    }

    @Test
    public void create_whenRequestorIsOwner_thenThrowException() {
        val newBookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        assertThrows(NotFoundException.class, () -> bookingService.create(newBookingCreateDto, owner.getId()));
    }

    @Test
    public void approve_whenOkAndApprove_thenReturnBooking() {
        val newBookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        val newBooking = bookingService.create(newBookingCreateDto, booker.getId());

        assertEquals(BookingStatus.WAITING, newBooking.getStatus());

        val bookingAfterApprove = bookingService.approve(newBooking.getId(), owner.getId(), true);

        assertEquals(newBooking.getId(), bookingAfterApprove.getId());
        assertEquals(BookingStatus.APPROVED, bookingAfterApprove.getStatus());
    }

    @Test
    public void approve_whenOkAndNotApprove_thenReturnBooking() {
        val newBookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        val newBooking = bookingService.create(newBookingCreateDto, booker.getId());

        assertEquals(BookingStatus.WAITING, newBooking.getStatus());

        val bookingAfterApprove = bookingService.approve(newBooking.getId(), owner.getId(), false);

        assertEquals(newBooking.getId(), bookingAfterApprove.getId());
        assertEquals(BookingStatus.REJECTED, bookingAfterApprove.getStatus());
    }

    @Test
    public void approve_whenStatusIsNotWaiting_thenThrowException() {
        var newBooking = Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.CANCELED)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        newBooking = bookingRepository.save(newBooking);

        assertNotEquals(BookingStatus.WAITING, newBooking.getStatus());

        val bookingId = newBooking.getId();
        assertThrows(UnsupportedOperationException.class, () -> bookingService.approve(bookingId, owner.getId(), true));
    }

    @Test
    public void getUserBookingsByState_whenSearchAll_thenReturnBookings() {
        bookingRepository.deleteAll();

        val bookingList = new ArrayList<>();
        val bookingCount = 3;
        for (int counter = 1; counter < bookingCount; counter++) {
            var booking = Booking.builder()
                    .item(item)
                    .booker(booker)
                    .status(BookingStatus.WAITING)
                    .start(LocalDateTime.now().plusDays(1 + counter))
                    .end(LocalDateTime.now().plusDays(3 + counter))
                    .build();
            booking = bookingRepository.save(booking);

            bookingList.add(booking);
        }

        val userBookings = bookingService.getUserBookingsByState(booker.getId(), BookingStateForSearch.ALL);
        assertFalse(userBookings.isEmpty());
        assertEquals(bookingList.size(), userBookings.size());
        assertThat(userBookings).isSortedAccordingTo(Comparator.comparing(BookingDto::getStart, LocalDateTime::compareTo).reversed());
    }

    @Test
    public void getUserBookingsByState_whenNoBookings_thenReturnEmpty() {
        bookingRepository.deleteAll();
        assertEquals(0, bookingRepository.count());

        val userBookings = bookingService.getUserBookingsByState(booker.getId(), BookingStateForSearch.ALL);
        assertTrue(userBookings.isEmpty());
    }

    @Test
    public void getUserBookingsByState_whenHasBookingsButOtherUser_thenReturnEmpty() {
        bookingRepository.deleteAll();

        var anotherBooker = User.builder()
                .name("anotherBooker")
                .email("anotherBooker@email.com")
                .build();
        anotherBooker = userRepository.save(anotherBooker);

        val bookingCount = 3;
        for (int counter = 1; counter < bookingCount; counter++) {
            var booking = Booking.builder()
                    .item(item)
                    .booker(anotherBooker)
                    .status(BookingStatus.WAITING)
                    .start(LocalDateTime.now().plusDays(1 + counter))
                    .end(LocalDateTime.now().plusDays(3 + counter))
                    .build();
            booking = bookingRepository.save(booking);
        }

        val userBookings = bookingService.getUserBookingsByState(booker.getId(), BookingStateForSearch.ALL);
        assertTrue(userBookings.isEmpty());
    }

    @Test
    public void getUserBookingsByState_whenHasPagination_B3From0Size5_thenReturnBookings() {
        bookingRepository.deleteAll();

        val bookingList = new ArrayList<>();
        val bookingCount = 3;
        for (int counter = 1; counter < bookingCount; counter++) {
            var booking = Booking.builder()
                    .item(item)
                    .booker(booker)
                    .status(BookingStatus.WAITING)
                    .start(LocalDateTime.now().plusDays(1 + counter))
                    .end(LocalDateTime.now().plusDays(3 + counter))
                    .build();
            booking = bookingRepository.save(booking);

            bookingList.add(booking);
        }

        val from = 0;
        val size = 5;
        val userBookings = bookingService.getUserBookingsByState(booker.getId(), BookingStateForSearch.ALL, from, size);
        assertFalse(userBookings.isEmpty());
        assertEquals(bookingList.size(), userBookings.size());
        assertThat(userBookings).isSortedAccordingTo(Comparator.comparing(BookingDto::getStart, LocalDateTime::compareTo).reversed());
    }

    @Test
    public void getUserBookingsByState_whenHasPagination_B10From3Size4_thenReturnBookings() {
        bookingRepository.deleteAll();

        val bookingList = new ArrayList<>();
        val bookingCount = 10;
        for (int counter = 1; counter < bookingCount; counter++) {
            var booking = Booking.builder()
                    .item(item)
                    .booker(booker)
                    .status(BookingStatus.WAITING)
                    .start(LocalDateTime.now().plusDays(1 + counter))
                    .end(LocalDateTime.now().plusDays(3 + counter))
                    .build();
            booking = bookingRepository.save(booking);

            bookingList.add(booking);
        }

        val from = 3;
        val size = 4;
        val userBookings = bookingService.getUserBookingsByState(booker.getId(), BookingStateForSearch.ALL, from, size);
        assertFalse(userBookings.isEmpty());
        assertEquals(size, userBookings.size());
        assertThat(userBookings).isSortedAccordingTo(Comparator.comparing(BookingDto::getStart, LocalDateTime::compareTo).reversed());
    }

    // Множество других проверок уже есть в тестах для репозитория.

    @AfterEach
    public void clean() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }
}
