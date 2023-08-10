package ru.practicum.shareit.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.DaoItem;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.DaoUser;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingRepositoryTest {
    private final BookingRepository bookingRepository;
    private final DaoUser daoUser;
    private final DaoItem daoItem;
    private TestEntityManager em;

    private User owner;
    private User notOwner;
    private User booker;
    private User notBooker;
    private Item item;

    @BeforeEach
    public void init() {
        owner = User.builder()
                .name("owner")
                .email("owner@email.com")
                .build();
        owner = daoUser.save(owner);

        notOwner = User.builder()
                .name("not owner")
                .email("notOwner@email.com")
                .build();
        notOwner = daoUser.save(notOwner);

        booker = User.builder()
                .name("booker")
                .email("booker@email.com")
                .build();
        booker = daoUser.save(booker);

        notBooker = User.builder()
                .name("not booker")
                .email("notNooker@email.com")
                .build();
        notBooker = daoUser.save(notBooker);

        item = Item.builder()
                .owner(owner)
                .name("Item")
                .description("description")
                .isAvailable(true)
                .build();
        item = daoItem.save(item);
    }

    @Test
    public void isUserOwnItemFromBooking_whenUserOwner_thenReturnTrue() {
        final LocalDateTime start = LocalDateTime.now().withNano(0).plusDays(1);
        final LocalDateTime end = start.plusDays(1);

        Booking booking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.WAITING)
                .start(start)
                .end(end)
                .build();
        booking = bookingRepository.save(booking);

        assertTrue(bookingRepository.isUserOwnItemFromBooking(booking.getId(), owner.getId()));
    }

    @Test
    public void isUserOwnItemFromBooking_whenUserNotOwner_thenReturnFalse() {
        final LocalDateTime start = LocalDateTime.now().withNano(0).plusDays(1);
        final LocalDateTime end = start.plusDays(1);

        Booking booking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.WAITING)
                .start(start)
                .end(end)
                .build();
        booking = bookingRepository.save(booking);

        assertFalse(bookingRepository.isUserOwnItemFromBooking(booking.getId(), notOwner.getId()));
    }

    @Test
    public void isUserOwnItemFromBooking_whenUserNotExists_thenReturnFalse() {
        final LocalDateTime start = LocalDateTime.now().withNano(0).plusDays(1);
        final LocalDateTime end = start.plusDays(1);

        Booking booking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.WAITING)
                .start(start)
                .end(end)
                .build();
        booking = bookingRepository.save(booking);

        final long userNotExistedId = 9999L;
        assertFalse(daoUser.existsById(userNotExistedId));
        assertFalse(bookingRepository.isUserOwnItemFromBooking(booking.getId(), userNotExistedId));
    }

    @Test
    public void isUserOwnItemFromBooking_whenBookingNotExists_thenReturnFalse() {
        final long bookingNotExistedId = 9999L;
        assertFalse(bookingRepository.existsById(bookingNotExistedId));
        assertFalse(bookingRepository.isUserOwnItemFromBooking(bookingNotExistedId, owner.getId()));
    }

    @Test
    public void isUserBookingAuthor_whenUserAuthor_thenReturnTrue() {
        final LocalDateTime start = LocalDateTime.now().withNano(0).plusDays(1);
        final LocalDateTime end = start.plusDays(1);

        Booking booking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.WAITING)
                .start(start)
                .end(end)
                .build();
        booking = bookingRepository.save(booking);

        assertTrue(bookingRepository.isUserBookingAuthor(booking.getId(), booker.getId()));
    }

    @Test
    public void isUserBookingAuthor_whenUserNotAuthor_thenReturnFalse() {
        final LocalDateTime start = LocalDateTime.now().withNano(0).plusDays(1);
        final LocalDateTime end = start.plusDays(1);

        Booking booking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.WAITING)
                .start(start)
                .end(end)
                .build();
        booking = bookingRepository.save(booking);

        assertFalse(bookingRepository.isUserBookingAuthor(booking.getId(), notBooker.getId()));
    }

    @Test
    public void getLastBookingForItemById_when_then() {

    }

    @Test
    public void getNextBookingForItemById_when_then() {

    }

    @Test
    public void getLastBookingForItemsByIdList_when_then() {

    }

    @Test
    public void getNextBookingForItemsByIdList_when_then() {

    }

    @Test
    public void isUserBookingItem_whenHeIs_thenReturnTrue() {
        final LocalDateTime start = LocalDateTime.now().withNano(0).plusDays(1);
        final LocalDateTime end = start.plusDays(1);

        Booking booking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(start)
                .end(end)
                .build();
        booking = bookingRepository.save(booking);

        assertTrue(bookingRepository.isUserBookingItem(booker.getId(), item.getId(), start.plusDays(10)));
    }

    @Test
    public void isUserBookingItem_whenHeIsAndFewBookings_thenReturnTrue() {
        final LocalDateTime start1 = LocalDateTime.now().withNano(0).plusDays(2);
        final LocalDateTime end1 = start1.plusDays(2);

        Booking booking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(start1)
                .end(end1)
                .build();
        bookingRepository.save(booking);

        final LocalDateTime start2 = LocalDateTime.now().withNano(0).plusDays(1);
        final LocalDateTime end2 = start1.plusDays(1);
        booking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.REJECTED)
                .start(start2)
                .end(end2)
                .build();
        bookingRepository.save(booking);

        assertTrue(bookingRepository.isUserBookingItem(booker.getId(), item.getId(), start1.plusDays(10)));
    }

    @Test
    public void isUserBookingItem_whenHeDont_thenReturnFalse() {
        final LocalDateTime start = LocalDateTime.now().withNano(0).plusDays(1);
        final LocalDateTime end = start.plusDays(1);

        Booking booking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(start)
                .end(end)
                .build();
        booking = bookingRepository.save(booking);

        assertFalse(bookingRepository.isUserBookingItem(notBooker.getId(), item.getId(), start.plusDays(10)));
    }

    @Test
    public void isUserBookingItem_whenHeIsButStatusNotApproved_thenReturnFalse() {
        final LocalDateTime start = LocalDateTime.now().withNano(0).plusDays(1);
        final LocalDateTime end = start.plusDays(1);

        Booking booking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.REJECTED)
                .start(start)
                .end(end)
                .build();
        booking = bookingRepository.save(booking);

        assertFalse(bookingRepository.isUserBookingItem(booker.getId(), item.getId(), start.plusDays(10)));
    }

    @Test
    public void isUserBookingItem_whenHeIsButStartInFuture_thenReturnFalse() {
        final LocalDateTime start = LocalDateTime.now().withNano(0).plusDays(1);
        final LocalDateTime end = start.plusDays(1);

        Booking booking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.REJECTED)
                .start(start)
                .end(end)
                .build();
        booking = bookingRepository.save(booking);

        assertFalse(bookingRepository.isUserBookingItem(booker.getId(), item.getId(), LocalDateTime.now()));
    }

    @AfterEach
    public void clean() {
        bookingRepository.deleteAll();
        daoItem.deleteAll();
        daoUser.deleteAll();
    }
}
