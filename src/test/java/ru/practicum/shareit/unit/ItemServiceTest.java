package ru.practicum.shareit.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.NotOwnerAccessException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.DaoItem;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.DaoUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    private final long ownerId = 1L;
    private final long userId = 2L;
    private final long requestorId = 3L;
    private final long itemId = 1L;
    private final long itemRequestId = 1L;
    private final User owner = User.builder()
            .id(ownerId)
            .name("Item owner")
            .email("itemOwner@email.com")
            .build();
    private final Item item = Item.builder()
            .id(itemId)
            .owner(owner)
            .name("Item")
            .description("item description")
            .isAvailable(true)
            .build();
    private final User user = User.builder()
            .id(userId)
            .name("user")
            .email("user@email.com")
            .build();

    private final User requestor = User.builder()
            .id(requestorId)
            .name("requestor")
            .email("requestor@email.com")
            .build();
    private final ItemRequest itemRequest = ItemRequest.builder()
            .id(itemRequestId)
            .requestor(requestor)
            .description("description")
            .created(LocalDateTime.now())
            .build();

    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private DaoUser daoUser;
    @Mock
    private DaoItem daoItem;
    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    public void create_whenOK_thenReturnId() {
        final ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.isAvailable())
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(owner);
        Mockito.when(daoItem.save(any(Item.class))).thenReturn(item);

        final long newItemId = itemService.create(itemCreateDto, ownerId);
        assertEquals(item.getId(), newItemId);

        verify(daoUser).getUserById(anyLong());
        verify(daoItem).save(any(Item.class));
    }

    @Test
    public void create_whenOwnerNotExists_thenThrowException() {
        final ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.isAvailable())
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenThrow(UserNotFoundException.class);

        assertThrows(UserNotFoundException.class, () -> itemService.create(itemCreateDto, ownerId));

        verify(daoUser).getUserById(anyLong());
        verify(daoItem, never()).save(any(Item.class));
    }

    @Test
    public void create_whenHasItemRequest_thenReturnId() {
        final ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.isAvailable())
                .requestId(itemRequestId)
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(owner);
        Mockito.when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        Mockito.when(daoItem.save(any(Item.class))).thenReturn(item);

        final long newItemId = itemService.create(itemCreateDto, ownerId);
        assertEquals(item.getId(), newItemId);

        verify(daoUser).getUserById(anyLong());
        verify(itemRequestRepository).findById(anyLong());
        verify(daoItem).save(any(Item.class));
    }

    @Test
    public void createAndGet_whenOk_thenReturnItem() {
        final ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.isAvailable())
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(owner);
        Mockito.when(daoItem.save(any(Item.class))).thenReturn(item);

        itemService.createAndGet(itemCreateDto, ownerId);

        verify(daoUser).getUserById(anyLong());
        verify(daoItem).save(any(Item.class));
    }

    @Test
    public void createAndGet_whenOwnerNotExists_thenThrowException() {
        final ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.isAvailable())
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenThrow(UserNotFoundException.class);

        assertThrows(UserNotFoundException.class, () -> itemService.createAndGet(itemCreateDto, ownerId));

        verify(daoUser).getUserById(anyLong());
        verify(daoItem, never()).save(any(Item.class));
    }

    @Test
    public void createAndGet_HasItemRequest_thenReturnItem() {
        final ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.isAvailable())
                .requestId(itemRequestId)
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(owner);
        Mockito.when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        Mockito.when(daoItem.save(any(Item.class))).thenReturn(item);

        itemService.createAndGet(itemCreateDto, ownerId);

        verify(daoUser).getUserById(anyLong());
        verify(itemRequestRepository).findById(anyLong());
        verify(daoItem).save(any(Item.class));
    }

    @Test
    public void update_whenOk_thenReturnItem() {
        final ItemDto itemDto = ItemDto.builder()
                .name("New item name")
                .description("new desc")
                .isAvailable(true)
                .build();

        doNothing().when(daoItem).checkItemExists(anyLong());
        doNothing().when(daoUser).checkUserExists(anyLong());
        Mockito.when(daoItem.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(true);
        Mockito.when(daoItem.getByIdAndOwnerId(anyLong(), anyLong())).thenReturn(item);
        Mockito.when(daoItem.save(any(Item.class))).thenReturn(item);

        final ItemDto updatedItem = itemService.update(itemId, itemDto, ownerId);

        verify(daoItem).checkItemExists(anyLong());
        verify(daoUser).checkUserExists(anyLong());
        verify(daoItem).existsByIdAndOwnerId(anyLong(), anyLong());
        verify(daoItem).getByIdAndOwnerId(anyLong(), anyLong());
        verify(daoItem).save(any(Item.class));
    }

    @Test
    public void update_whenNotOwnerTryUpdate_thenThrowException() {
        final ItemDto itemDto = ItemDto.builder()
                .name("New item name")
                .description("new desc")
                .isAvailable(true)
                .build();

        doNothing().when(daoItem).checkItemExists(anyLong());
        doNothing().when(daoUser).checkUserExists(anyLong());
        Mockito.when(daoItem.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(false);

        assertThrows(NotOwnerAccessException.class, () -> itemService.update(itemId, itemDto, ownerId));

        verify(daoItem).checkItemExists(anyLong());
        verify(daoUser).checkUserExists(anyLong());
        verify(daoItem).existsByIdAndOwnerId(anyLong(), anyLong());
        verify(daoItem, never()).getByIdAndOwnerId(anyLong(), anyLong());
        verify(daoItem, never()).save(any(Item.class));
    }

    @Test
    public void getById_whenOkNotOwner_thenReturnItem() {
        Mockito.when(daoItem.getItemById(anyLong())).thenReturn(item);
        Mockito.when(commentRepository.findByItemId(anyLong())).thenReturn(emptyList());

        itemService.getById(itemId, userId);

        verify(daoItem).getItemById(anyLong());
        verify(commentRepository).findByItemId(anyLong());
        verify(bookingRepository, never()).getLastBookingForItemById(anyLong(), any(LocalDateTime.class));
        verify(bookingRepository, never()).getNextBookingForItemById(anyLong(), any(LocalDateTime.class));
    }

    @Test
    public void getById_whenOkOwner_thenReturnItem() {
        final Booking lastBooking = Booking.builder()
                .id(1L)
                .booker(user)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .build();

        final Booking nextBooking = Booking.builder()
                .id(2L)
                .booker(user)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .build();

        Mockito.when(daoItem.getItemById(anyLong())).thenReturn(item);
        Mockito.when(commentRepository.findByItemId(anyLong())).thenReturn(emptyList());
        Mockito.when(bookingRepository.getLastBookingForItemById(anyLong(), any(LocalDateTime.class))).thenReturn(lastBooking);
        Mockito.when(bookingRepository.getNextBookingForItemById(anyLong(), any(LocalDateTime.class))).thenReturn(nextBooking);

        itemService.getById(itemId, ownerId);

        verify(daoItem).getItemById(anyLong());
        verify(commentRepository).findByItemId(anyLong());
        verify(bookingRepository).getLastBookingForItemById(anyLong(), any(LocalDateTime.class));
        verify(bookingRepository).getNextBookingForItemById(anyLong(), any(LocalDateTime.class));
    }

    @Test
    public void getOwnerItemById_whenOk_thenReturnItem() {
        doNothing().when(daoItem).checkItemExists(anyLong());
        doNothing().when(daoUser).checkUserExists(anyLong());
        Mockito.when(daoItem.getByIdAndOwnerId(anyLong(), anyLong())).thenReturn(any(Item.class));

        itemService.getOwnerItemById(itemId, ownerId);

        verify(daoItem).checkItemExists(anyLong());
        verify(daoUser).checkUserExists(anyLong());
        verify(daoItem).getByIdAndOwnerId(anyLong(), anyLong());
    }

    @Test
    public void getOwnerItemById_whenItemNotExisted_thenThrowException() {
        doThrow(ItemNotFoundException.class).when(daoItem).checkItemExists(anyLong());

        assertThrows(ItemNotFoundException.class, () -> itemService.getOwnerItemById(itemId, ownerId));

        verify(daoItem).checkItemExists(anyLong());
        verify(daoUser, never()).checkUserExists(anyLong());
        verify(daoItem, never()).getByIdAndOwnerId(anyLong(), anyLong());
    }

    @Test
    public void getAllOwnerItems_whenOk_thenReturnItems() {
        doNothing().when(daoUser).checkUserExists(anyLong());
        Mockito.when(daoItem.findByOwnerId(ownerId, null)).thenReturn(List.of(item));
        Mockito.when(bookingRepository.getLastBookingForItemsByIdList(anyList(), any(LocalDateTime.class)))
                .thenReturn(emptyMap());
        Mockito.when(bookingRepository.getNextBookingForItemsByIdList(anyList(), any(LocalDateTime.class)))
                .thenReturn(emptyMap());
        Mockito.when(commentRepository.findByItemIdIn(anyList(), any(Sort.class))).thenReturn(emptyList());

        itemService.getAllOwnerItems(ownerId);

        verify(daoUser).checkUserExists(anyLong());
        verify(daoItem).findByOwnerId(ownerId, null);
        verify(bookingRepository).getLastBookingForItemsByIdList(anyList(), any(LocalDateTime.class));
        verify(bookingRepository).getNextBookingForItemsByIdList(anyList(), any(LocalDateTime.class));
        verify(commentRepository).findByItemIdIn(anyList(), any(Sort.class));
    }

    @Test
    public void getAllOwnerItems_whenNoItems_thenReturnEmpty() {
        doNothing().when(daoUser).checkUserExists(anyLong());
        Mockito.when(daoItem.findByOwnerId(ownerId, null)).thenReturn(emptyList());

        final List<ItemWithAdditionalDataDto> emptyCollection = itemService.getAllOwnerItems(ownerId);
        assertTrue(emptyCollection.isEmpty());

        verify(daoUser).checkUserExists(anyLong());
        verify(daoItem).findByOwnerId(ownerId, null);
        verify(bookingRepository, never()).getLastBookingForItemsByIdList(anyList(), any(LocalDateTime.class));
        verify(bookingRepository, never()).getNextBookingForItemsByIdList(anyList(), any(LocalDateTime.class));
        verify(commentRepository, never()).findByItemIdIn(anyList(), any(Sort.class));
    }

    @Test
    public void searchItems_whenOk_thenReturnItems() {
        final String searchText = "search";

        doNothing().when(daoUser).checkUserExists(anyLong());
        Mockito.when(daoItem.findAvailableByNameOrDescription(searchText, null)).thenReturn(emptyList());

        final List<ItemDto> items = itemService.searchItems(searchText, userId);

        verify(daoUser).checkUserExists(anyLong());
        verify(daoItem).findAvailableByNameOrDescription(searchText, null);
    }

    @Test
    public void searchItems_whenSearchTextIsBlank_thenReturnEmptyList() {
        doNothing().when(daoUser).checkUserExists(anyLong());
        final List<ItemDto> items = itemService.searchItems("", userId);

        assertTrue(items.isEmpty());

        verify(daoUser).checkUserExists(anyLong());
        verify(daoItem, never()).findAvailableByNameOrDescription(anyString(), any(Pageable.class));
    }

    @Test
    public void searchItems_whenOkWithPagination_thenReturnItems() {
        final String searchText = "search";

        doNothing().when(daoUser).checkUserExists(anyLong());
        Mockito.when(daoItem.findAvailableByNameOrDescription(anyString(), any(Pageable.class))).thenReturn(emptyList());

        final Integer from = 0;
        final Integer size = 5;
        final List<ItemDto> items = itemService.searchItems(searchText, userId, from, size);

        verify(daoUser).checkUserExists(anyLong());
        verify(daoItem).findAvailableByNameOrDescription(anyString(), any(Pageable.class));
    }

    @Test
    public void addComment_whenOk_thenReturnCommentDto() {
        final CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .text("comment text")
                .build();
        final Comment comment = Comment.builder()
                .author(user)
                .text(commentCreateDto.getText())
                .build();

        Mockito.when(daoItem.getItemById(anyLong())).thenReturn(item);
        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(user);
        Mockito.when(bookingRepository.isUserBookingItem(anyLong(), anyLong(), any(LocalDateTime.class))).thenReturn(true);
        Mockito.when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        final CommentDto commentDto = itemService.addComment(itemId, userId, commentCreateDto);
        assertEquals(comment.getText(), commentDto.getText());

        verify(daoUser).getUserById(anyLong());
        verify(daoItem).getItemById(anyLong());
        verify(bookingRepository).isUserBookingItem(anyLong(), anyLong(), any(LocalDateTime.class));
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    public void addComment_whenUserNotBooker_thenThrowException() {
        final CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .text("comment text")
                .build();

        Mockito.when(daoItem.getItemById(anyLong())).thenReturn(item);
        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(user);
        Mockito.when(bookingRepository.isUserBookingItem(anyLong(), anyLong(), any(LocalDateTime.class))).thenReturn(false);

        assertThrows(UnsupportedOperationException.class, () -> itemService.addComment(itemId, userId, commentCreateDto));

        verify(daoUser).getUserById(anyLong());
        verify(daoItem).getItemById(anyLong());
        verify(bookingRepository).isUserBookingItem(anyLong(), anyLong(), any(LocalDateTime.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    public void addComment_whenUserNotExists_thenThrowException() {
        final CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .text("comment text")
                .build();

        Mockito.when(daoItem.getItemById(anyLong())).thenReturn(item);
        Mockito.when(daoUser.getUserById(anyLong())).thenThrow(UserNotFoundException.class);

        assertThrows(UserNotFoundException.class, () -> itemService.addComment(itemId, userId, commentCreateDto));

        verify(daoUser).getUserById(anyLong());
        verify(daoItem).getItemById(anyLong());
        verify(bookingRepository, never()).isUserBookingItem(anyLong(), anyLong(), any(LocalDateTime.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    public void addComment_whenItemNotExists_thenThrowException() {
        final CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .text("comment text")
                .build();

        Mockito.when(daoItem.getItemById(anyLong())).thenThrow(ItemNotFoundException.class);

        assertThrows(ItemNotFoundException.class, () -> itemService.addComment(itemId, userId, commentCreateDto));

        verify(daoUser, never()).getUserById(anyLong());
        verify(daoItem).getItemById(anyLong());
        verify(bookingRepository, never()).isUserBookingItem(anyLong(), anyLong(), any(LocalDateTime.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }
}
