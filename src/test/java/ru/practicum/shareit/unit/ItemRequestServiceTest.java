package ru.practicum.shareit.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.common.OffsetBasedPageRequest;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.DaoUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {
    private final long userId = 1L;
    private final long requestId = 1L;
    private final User requestor = User.builder()
            .id(userId)
            .name("requestor")
            .email("requestor@email.com")
            .build();
    private final ItemRequest itemRequest = ItemRequest.builder()
            .id(requestId)
            .requestor(requestor)
            .description("description")
            .created(LocalDateTime.now())
            .build();
    private final Sort itemRequestCreatedSort = Sort.by("created");
    @Mock
    private DaoUser daoUser;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    public void create_whenOk_thenReturnId() {
        final ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder()
                .description(itemRequest.getDescription())
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(requestor);
        Mockito.when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        final long requestId = itemRequestService.create(itemRequestCreateDto, userId);

        verify(daoUser).getUserById(anyLong());
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    public void create_whenUserNotExisted_thenThrowException() {
        final ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder()
                .description(itemRequest.getDescription())
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenThrow(UserNotFoundException.class);

        assertThrows(UserNotFoundException.class, () -> itemRequestService.create(itemRequestCreateDto, userId));

        verify(daoUser).getUserById(anyLong());
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    public void createAndGet_whenOk_thenReturnItemRequest() {
        final ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder()
                .description(itemRequest.getDescription())
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenReturn(requestor);
        Mockito.when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        final ItemRequestDto itemRequestDto = itemRequestService.createAndGet(itemRequestCreateDto, userId);

        verify(daoUser).getUserById(anyLong());
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    public void createAndGet_whenUserNotExisted_thenThrowException() {
        final ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder()
                .description(itemRequest.getDescription())
                .build();

        Mockito.when(daoUser.getUserById(anyLong())).thenThrow(UserNotFoundException.class);

        assertThrows(UserNotFoundException.class, () -> itemRequestService.createAndGet(itemRequestCreateDto, userId));

        verify(daoUser).getUserById(anyLong());
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    public void getAllUserItemRequests_whenOk_thenReturnItemRequests() {
        doNothing().when(daoUser).checkUserExists(anyLong());

        final List<ItemRequestDto> requestDtoList = itemRequestService.getAllUserItemRequests(userId);

        verify(daoUser).checkUserExists(anyLong());
        verify(itemRequestRepository).findByRequestorId(userId, itemRequestCreatedSort.descending());
    }

    @Test
    public void getAllUserItemRequests_whenUserNotExisted_thenReturnItemRequests() {
        doThrow(UserNotFoundException.class).when(daoUser).checkUserExists(anyLong());

        assertThrows(UserNotFoundException.class, () -> itemRequestService.getAllUserItemRequests(userId));

        verify(daoUser).checkUserExists(anyLong());
        verify(itemRequestRepository, never()).findByRequestorId(userId, itemRequestCreatedSort.descending());
    }

    @Test
    public void getAllItemRequests_whenOk_thenReturnItemRequest() {
        doNothing().when(daoUser).checkUserExists(anyLong());

        itemRequestService.getAllItemRequests(userId, null, null);

        verify(daoUser).checkUserExists(anyLong());
        verify(itemRequestRepository).findByRequestorIdNot(userId, itemRequestCreatedSort.descending());
    }

    @Test
    public void getAllItemRequests_whenUserNotExisted_thenThrowException() {
        doThrow(UserNotFoundException.class).when(daoUser).checkUserExists(anyLong());

        assertThrows(UserNotFoundException.class, () -> itemRequestService.getAllItemRequests(userId, null, null));

        verify(daoUser).checkUserExists(anyLong());
        verify(itemRequestRepository, never()).findByRequestorIdNot(userId, itemRequestCreatedSort.descending());
    }

    @Test
    public void getAllItemRequests_whenOkWithPagination_thenItemRequest() {
        doNothing().when(daoUser).checkUserExists(anyLong());

        final Integer from = 0;
        final Integer size = 10;
        itemRequestService.getAllItemRequests(userId, from, size);

        verify(daoUser).checkUserExists(anyLong());
        verify(itemRequestRepository).findByRequestorIdNot(userId, new OffsetBasedPageRequest(from, size, itemRequestCreatedSort.descending()));
    }

    @Test
    public void getItemRequestById_whenOk_thenReturnItemRequest() {
        Mockito.when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));

        itemRequestService.getItemRequestById(requestId);

        verify(itemRequestRepository).findById(anyLong());
    }

    @Test
    public void getItemRequestById_whenItemRequestNotExisted_thenThrowException() {
        Mockito.when(itemRequestRepository.findById(anyLong())).thenThrow(ItemRequestNotFoundException.class);

        assertThrows(ItemRequestNotFoundException.class, () -> itemRequestService.getItemRequestById(requestId));

        verify(itemRequestRepository).findById(anyLong());
    }
}
