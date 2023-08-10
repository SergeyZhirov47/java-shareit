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
import ru.practicum.shareit.booking.model.BookingStateForSearch;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.DaoItem;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.DaoUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void getUserBookingsByState_whenNoBookings_thenReturnEmpty() {
        final List<Booking> bookings = bookingRepository.getUserBookingsByState(booker.getId(), BookingStateForSearch.ALL);
        assertTrue(bookings.isEmpty());
    }

    @Test
    public void getUserBookingsByState_whenStateIsAll_thenReturnAll() {
        final LocalDateTime start = LocalDateTime.now().withNano(0).plusDays(1);
        final LocalDateTime end = start.plusDays(1);

        final List<Booking> bookingList = new ArrayList<>();
        for (BookingStatus status : BookingStatus.values()) {
            Booking booking = Booking.builder()
                    .booker(booker)
                    .item(item)
                    .status(status)
                    .start(start)
                    .end(end)
                    .build();
            booking = bookingRepository.save(booking);

            bookingList.add(booking);
        }

        final List<Booking> bookings = bookingRepository.getUserBookingsByState(booker.getId(), BookingStateForSearch.ALL);
        assertFalse(bookings.isEmpty());
        assertEquals(bookingList.size(), bookings.size());
        assertEquals(bookingList, bookings);
    }

    @Test
    public void getUserBookingsByState_whenStateIsWAITING_thenReturnWaiting() {
        final LocalDateTime start = LocalDateTime.now().withNano(0).plusDays(1);
        final LocalDateTime end = start.plusDays(1);

        Booking bookingWaitingStatus = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.WAITING)
                .start(start)
                .end(end)
                .build();
        bookingWaitingStatus = bookingRepository.save(bookingWaitingStatus);

        Booking booking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(start)
                .end(end)
                .build();
        booking = bookingRepository.save(booking);

        final List<Booking> expectedResult = List.of(bookingWaitingStatus);
        final List<Booking> bookings = bookingRepository.getUserBookingsByState(booker.getId(), BookingStateForSearch.WAITING);

        assertFalse(bookings.isEmpty());
        assertEquals(expectedResult.size(), bookings.size());
        assertEquals(expectedResult, bookings);
        assertEquals(bookingWaitingStatus.getStatus(), bookings.get(0).getStatus());
    }

    @Test
    public void getUserBookingsByState_whenStateIsREJECTED_thenReturnRejected() {
        final LocalDateTime start = LocalDateTime.now().withNano(0).plusDays(1);
        final LocalDateTime end = start.plusDays(1);

        Booking bookingRegectedStatus = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.REJECTED)
                .start(start)
                .end(end)
                .build();
        bookingRegectedStatus = bookingRepository.save(bookingRegectedStatus);

        Booking booking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(start)
                .end(end)
                .build();
        booking = bookingRepository.save(booking);

        final List<Booking> expectedResult = List.of(bookingRegectedStatus);
        final List<Booking> bookings = bookingRepository.getUserBookingsByState(booker.getId(), BookingStateForSearch.REJECTED);

        assertFalse(bookings.isEmpty());
        assertEquals(expectedResult.size(), bookings.size());
        assertEquals(expectedResult, bookings);
        assertEquals(bookingRegectedStatus.getStatus(), bookings.get(0).getStatus());
    }

    @Test
    public void getUserBookingsByState_whenStateIsFUTURE_thenReturnFuture() {
        final LocalDateTime startPast = LocalDateTime.now().withNano(0).minusDays(10);
        final LocalDateTime endPast = startPast.minusDays(5);

        final LocalDateTime startFuture = LocalDateTime.now().withNano(0).plusDays(10);
        final LocalDateTime endFuture = startFuture.plusDays(10);

        Booking pastBooking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(startPast)
                .end(endPast)
                .build();
        pastBooking = bookingRepository.save(pastBooking);

        Booking futureBooking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(startFuture)
                .end(endFuture)
                .build();
        futureBooking = bookingRepository.save(futureBooking);

        final List<Booking> expectedResult = List.of(futureBooking);
        final List<Booking> bookings = bookingRepository.getUserBookingsByState(booker.getId(), BookingStateForSearch.FUTURE);

        assertFalse(bookings.isEmpty());
        assertEquals(expectedResult.size(), bookings.size());
        assertEquals(expectedResult, bookings);
    }

    @Test
    public void getUserBookingsByState_whenStateIsCURRENT_thenReturnCurrent() {
        final LocalDateTime startPast = LocalDateTime.now().withNano(0).minusDays(10);
        final LocalDateTime endPast = startPast.minusDays(5);

        final LocalDateTime startFuture = LocalDateTime.now().withNano(0).plusDays(10);
        final LocalDateTime endFuture = startFuture.plusDays(10);

        final LocalDateTime startCurrent = LocalDateTime.now().withNano(0).minusDays(1);
        final LocalDateTime endCurrent = startFuture.plusDays(1);

        Booking pastBooking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(startPast)
                .end(endPast)
                .build();
        pastBooking = bookingRepository.save(pastBooking);

        Booking futureBooking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(startFuture)
                .end(endFuture)
                .build();
        futureBooking = bookingRepository.save(futureBooking);

        Booking currentBooking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(startCurrent)
                .end(endCurrent)
                .build();
        currentBooking = bookingRepository.save(currentBooking);

        final List<Booking> expectedResult = List.of(currentBooking);
        final List<Booking> bookings = bookingRepository.getUserBookingsByState(booker.getId(), BookingStateForSearch.CURRENT);

        assertFalse(bookings.isEmpty());
        assertEquals(expectedResult.size(), bookings.size());
        assertEquals(expectedResult, bookings);
    }

    @Test
    public void getUserBookingsByState_whenStateIsPAST_thenReturnPast() {
        final LocalDateTime startPast = LocalDateTime.now().withNano(0).minusDays(10);
        final LocalDateTime endPast = startPast.minusDays(5);

        final LocalDateTime startFuture = LocalDateTime.now().withNano(0).plusDays(10);
        final LocalDateTime endFuture = startFuture.plusDays(10);

        Booking pastBooking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.REJECTED)
                .start(startPast)
                .end(endPast)
                .build();
        pastBooking = bookingRepository.save(pastBooking);

        Booking futureBooking = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(startFuture)
                .end(endFuture)
                .build();
        futureBooking = bookingRepository.save(futureBooking);

        final List<Booking> expectedResult = List.of(pastBooking);
        final List<Booking> bookings = bookingRepository.getUserBookingsByState(booker.getId(), BookingStateForSearch.PAST);

        assertFalse(bookings.isEmpty());
        assertEquals(expectedResult.size(), bookings.size());
        assertEquals(expectedResult, bookings);
    }

    @Test
    public void getBookingsByItemOwner_whenStateIsAll_thenReturnBookings() {
        final LocalDateTime start = LocalDateTime.now().withNano(0).plusDays(1);
        final LocalDateTime end = start.plusDays(1);

        Booking booking1 = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(start)
                .end(end)
                .build();
        booking1 = bookingRepository.save(booking1);

        Booking booking2 = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.WAITING)
                .start(start)
                .end(end)
                .build();
        booking2 = bookingRepository.save(booking2);

        final List<Booking> expectedResult = List.of(booking1, booking2);
        final List<Booking> bookings = bookingRepository.getBookingsByItemOwner(owner.getId(), BookingStateForSearch.ALL);

        assertFalse(bookings.isEmpty());
        assertEquals(expectedResult.size(), bookings.size());
        assertEquals(expectedResult, bookings);
    }

    @Test
    public void getBookingsByItemOwner_whenStateIsWaiting_thenReturnWaiting() {
        final LocalDateTime start = LocalDateTime.now().withNano(0).plusDays(1);
        final LocalDateTime end = start.plusDays(1);

        Booking booking1 = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(start)
                .end(end)
                .build();
        booking1 = bookingRepository.save(booking1);

        Booking bookingWaitingStatus = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.WAITING)
                .start(start)
                .end(end)
                .build();
        bookingWaitingStatus = bookingRepository.save(bookingWaitingStatus);

        final List<Booking> expectedResult = List.of(bookingWaitingStatus);
        final List<Booking> bookings = bookingRepository.getBookingsByItemOwner(owner.getId(), BookingStateForSearch.WAITING);

        assertFalse(bookings.isEmpty());
        assertEquals(expectedResult.size(), bookings.size());
        assertEquals(expectedResult, bookings);
    }

    @Test
    public void getBookingsByItemOwner_whenStateIsAllAndOtherBookings_thenReturnOnlyForOwner() {
        final LocalDateTime start = LocalDateTime.now().withNano(0).plusDays(1);
        final LocalDateTime end = start.plusDays(1);

        Booking bookingOwner1 = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(start)
                .end(end)
                .build();
        bookingOwner1 = bookingRepository.save(bookingOwner1);

        Booking bookingOwner2 = Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.WAITING)
                .start(start)
                .end(end)
                .build();
        bookingOwner2 = bookingRepository.save(bookingOwner2);

        final User anotherOwner = User.builder()
                .name("another owner")
                .email("anotherOwner@email.com")
                .build();
        daoUser.save(anotherOwner);
        final Item anotherItem = Item.builder()
                .owner(anotherOwner)
                .name("Another item")
                .description("description")
                .isAvailable(true)
                .build();
        daoItem.save(anotherItem);

        Booking bookingAnotherOwner = Booking.builder()
                .booker(booker)
                .item(anotherItem)
                .status(BookingStatus.APPROVED)
                .start(start)
                .end(end)
                .build();
        bookingAnotherOwner = bookingRepository.save(bookingAnotherOwner);

        final List<Booking> expectedResult = List.of(bookingOwner1, bookingOwner2);
        final List<Booking> bookings = bookingRepository.getBookingsByItemOwner(owner.getId(), BookingStateForSearch.ALL);

        assertFalse(bookings.isEmpty());
        assertEquals(expectedResult.size(), bookings.size());
        assertEquals(expectedResult, bookings);
        assertThat(bookings).doesNotContain(bookingAnotherOwner);
    }

    @AfterEach
    public void clean() {
        bookingRepository.deleteAll();
        daoItem.deleteAll();
        daoUser.deleteAll();
    }
}
