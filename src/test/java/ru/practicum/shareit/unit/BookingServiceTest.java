package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStateForSearch;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.common.NotFoundException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.DaoItem;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.DaoUser;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    private final long itemId = 1L;
    private final LocalDateTime start = LocalDateTime.now().withNano(0).plusDays(1);
    private final LocalDateTime end = start.plusDays(1);
    private final long bookerId = 1L;
    private final long bookingId = 1L;
    private final long ownerId = 2L;
    private User booker;
    private User owner;
    private Item item;
    private Booking booking;
    @Mock
    private DaoUser daoUser;
    @Mock
    private DaoItem daoItem;
    @Mock
    private BookingRepository bookingRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;

    @BeforeEach
    public void init() {
        booker = User.builder()
                .id(bookerId)
                .name("booker")
                .email("booker@email.com")
                .build();

        owner = User.builder()
                .id(ownerId)
                .name("owner")
                .email("owner@email.com")
                .build();

        item = Item.builder()
                .id(itemId)
                .name("Item")
                .description("description")
                .owner(owner)
                .isAvailable(true)
                .build();

        booking = Booking.builder()
                .id(bookingId)
                .booker(booker)
                .item(item)
                .status(BookingStatus.WAITING)
                .start(start)
                .end(end)
                .build();
    }

    @Test
    public void create_whenOK_thenReturnBooking() {
        final BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(booker);
        Mockito.when(daoItem.getItemById(anyLong())).thenReturn(item);
        Mockito.when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        bookingService.create(bookingCreateDto, bookerId);

        verify(daoUser).getUserById(anyLong());
        verify(daoItem).getItemById(anyLong());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    public void create_whenBookerNotExisted_thenThrowException() {
        final BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenThrow(UserNotFoundException.class);

        assertThrows(UserNotFoundException.class, () -> bookingService.create(bookingCreateDto, bookerId));

        verify(daoUser).getUserById(anyLong());
        verify(daoItem, never()).getItemById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void create_whenItemNotExisted_thenThrowException() {
        final BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(booker);
        Mockito.when(daoItem.getItemById(anyLong())).thenThrow(ItemNotFoundException.class);

        assertThrows(ItemNotFoundException.class, () -> bookingService.create(bookingCreateDto, bookerId));

        verify(daoUser).getUserById(anyLong());
        verify(daoItem).getItemById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void create_whenBookerIsOwner_thenThrowException() {
        final BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(owner);
        Mockito.when(daoItem.getItemById(anyLong())).thenReturn(item);

        assertThrows(NotFoundException.class, () -> bookingService.create(bookingCreateDto, ownerId));

        verify(daoUser).getUserById(anyLong());
        verify(daoItem).getItemById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void create_whenItemUnavailable_thenThrowException() {
        final BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        item.setAvailable(false);

        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(booker);
        Mockito.when(daoItem.getItemById(anyLong())).thenReturn(item);

        assertThrows(UnsupportedOperationException.class, () -> bookingService.create(bookingCreateDto, bookerId));

        verify(daoUser).getUserById(anyLong());
        verify(daoItem).getItemById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void approve_whenOk_thenReturnBooking() {
        Mockito.when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.isUserOwnItemFromBooking(anyLong(), anyLong())).thenReturn(true);
        Mockito.when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        bookingService.approve(bookingId, ownerId, true);

        verify(bookingRepository).isUserOwnItemFromBooking(anyLong(), anyLong());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    public void approve_whenBookingNotWaitingStatus_thenThrowException() {
        Mockito.when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        booking.setStatus(BookingStatus.CANCELED);

        assertThrows(UnsupportedOperationException.class, () -> bookingService.approve(bookingId, ownerId, true));

        verify(bookingRepository, never()).isUserOwnItemFromBooking(anyLong(), anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void approve_whenTryApproveNotOwner_thenThrowException() {
        Mockito.when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.isUserOwnItemFromBooking(anyLong(), anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.approve(bookingId, anyLong(), true));

        verify(bookingRepository).isUserOwnItemFromBooking(anyLong(), anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void getBooking_whenGettingOwner_thenReturnBooking() {
        doNothing().when(daoUser).checkUserExists(anyLong());
        Mockito.when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.isUserOwnItemFromBooking(anyLong(), anyLong())).thenReturn(true);
        Mockito.when(bookingRepository.isUserBookingAuthor(anyLong(), anyLong())).thenReturn(false);

        bookingService.getBooking(bookingId, ownerId);

        verify(daoUser).checkUserExists(anyLong());
        verify(bookingRepository).findById(anyLong());
        verify(bookingRepository).isUserOwnItemFromBooking(anyLong(), anyLong());
        verify(bookingRepository).isUserBookingAuthor(anyLong(), anyLong());
    }

    @Test
    public void getBooking_whenGettingBooker_thenReturnBooking() {
        doNothing().when(daoUser).checkUserExists(anyLong());
        Mockito.when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.isUserOwnItemFromBooking(anyLong(), anyLong())).thenReturn(false);
        Mockito.when(bookingRepository.isUserBookingAuthor(anyLong(), anyLong())).thenReturn(true);

        bookingService.getBooking(bookingId, bookerId);

        verify(daoUser).checkUserExists(anyLong());
        verify(bookingRepository).findById(anyLong());
        verify(bookingRepository).isUserOwnItemFromBooking(anyLong(), anyLong());
        verify(bookingRepository).isUserBookingAuthor(anyLong(), anyLong());
    }

    @Test
    public void getBooking_whenUserNotExisted_thenThrowException() {
        doThrow(UserNotFoundException.class).when(daoUser).checkUserExists(anyLong());

        assertThrows(UserNotFoundException.class, () -> bookingService.getBooking(bookingId, bookerId));

        verify(daoUser).checkUserExists(anyLong());
        verify(bookingRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).isUserOwnItemFromBooking(anyLong(), anyLong());
        verify(bookingRepository, never()).isUserBookingAuthor(anyLong(), anyLong());
    }

    @Test
    public void getBooking_whenTryGetNotOwnerOrAuthor_thenThrowException() {
        doNothing().when(daoUser).checkUserExists(anyLong());
        Mockito.when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.isUserOwnItemFromBooking(anyLong(), anyLong())).thenReturn(false);
        Mockito.when(bookingRepository.isUserBookingAuthor(anyLong(), anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.getBooking(bookingId, bookerId));

        verify(daoUser).checkUserExists(anyLong());
        verify(bookingRepository).findById(anyLong());
        verify(bookingRepository).isUserOwnItemFromBooking(anyLong(), anyLong());
        verify(bookingRepository).isUserBookingAuthor(anyLong(), anyLong());
    }

    @Test
    public void getUserBookingsByState_whenOk_thenReturnBookings() {
        doNothing().when(daoUser).checkUserExists(anyLong());

        bookingService.getUserBookingsByState(bookerId, BookingStateForSearch.ALL);

        verify(daoUser).checkUserExists(anyLong());
        verify(bookingRepository).getUserBookingsByState(anyLong(), any(BookingStateForSearch.class));
    }

    @Test
    public void getUserBookingsByState_whenUserNotFound_thenThrowException() {
        doThrow(UserNotFoundException.class).when(daoUser).checkUserExists(anyLong());

        assertThrows(UserNotFoundException.class, () -> bookingService.getUserBookingsByState(bookerId, BookingStateForSearch.ALL));

        verify(daoUser).checkUserExists(anyLong());
        verify(bookingRepository, never()).getUserBookingsByState(anyLong(), any(BookingStateForSearch.class));
    }

    @Test
    public void getUserBookingsByState_whenOkWithPageable_thenReturnBookings() {
        doNothing().when(daoUser).checkUserExists(anyLong());

        final Integer from = 0;
        final Integer size = 10;
        bookingService.getUserBookingsByState(bookerId, BookingStateForSearch.ALL, from, size);

        verify(daoUser).checkUserExists(anyLong());
        verify(bookingRepository).getUserBookingsByState(anyLong(), any(BookingStateForSearch.class), any(Pageable.class));
    }

    @Test
    public void getBookingsByItemOwner_whenOk_thenReturnBookings() {
        doNothing().when(daoUser).checkUserExists(anyLong());

        bookingService.getBookingsByItemOwner(ownerId, BookingStateForSearch.ALL);

        verify(daoUser).checkUserExists(anyLong());
        verify(bookingRepository).getBookingsByItemOwner(anyLong(), any(BookingStateForSearch.class));
    }

    @Test
    public void getBookingsByItemOwner_when_thenThrowException() {
        doThrow(UserNotFoundException.class).when(daoUser).checkUserExists(anyLong());

        assertThrows(UserNotFoundException.class, () -> bookingService.getBookingsByItemOwner(ownerId, BookingStateForSearch.ALL));

        verify(daoUser).checkUserExists(anyLong());
        verify(bookingRepository, never()).getBookingsByItemOwner(anyLong(), any(BookingStateForSearch.class));
    }

    @Test
    public void getBookingsByItemOwner_whenOkWithPageable_thenReturnBookings() {
        doNothing().when(daoUser).checkUserExists(anyLong());

        final Integer from = 0;
        final Integer size = 10;
        bookingService.getBookingsByItemOwner(ownerId, BookingStateForSearch.ALL, from, size);

        verify(daoUser).checkUserExists(anyLong());
        verify(bookingRepository).getBookingsByItemOwner(anyLong(), any(BookingStateForSearch.class), any(Pageable.class));
    }
}
