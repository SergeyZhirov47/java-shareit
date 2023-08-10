package ru.practicum.shareit.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.DaoItem;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.DaoUser;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    private final long ownerId = 1L;
    private final long userId = 2L;
    private final long itemId = 1L;
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
    public void createAndGet() {

    }

    // ToDo
    // create и createAndGet с itemRequest om

    @Test
    public void update() {

    }

    @Test
    public void getById() {

    }

    @Test
    public void getOwnerItemById() {

    }

    @Test
    public void getAllOwnerItems() {

    }

    @Test
    public void searchItems_when_then() {

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
