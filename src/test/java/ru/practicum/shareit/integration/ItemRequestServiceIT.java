package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemRequestServiceIT {
    private final ItemRequestService itemRequestService;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final Comparator<ItemRequestDto> itemRequestCreatedDescComparator = Comparator
            .comparing(ItemRequestDto::getCreated, LocalDateTime::compareTo)
            .reversed();
    private User requestor;
    private User otherRequestor;

    @BeforeEach
    public void init() {
        requestor = User.builder()
                .name("requestor")
                .email("requestor@email.com")
                .build();
        requestor = userRepository.save(requestor);

        otherRequestor = User.builder()
                .name("otherRequestor")
                .email("otherRequestor@email.com")
                .build();
        otherRequestor = userRepository.save(otherRequestor);
    }

    @Test
    public void getAllUserItemRequests_whenOneItemRequest_thenReturnItemRequests() {
        assertEquals(0, itemRequestRepository.count());

        val itemRequestCreateDto = ItemRequestCreateDto.builder()
                .description("need item")
                .build();

        val itemRequest = itemRequestService.createAndGet(itemRequestCreateDto, requestor.getId());
        assertEquals(1, itemRequestRepository.count());

        val userItemRequests = itemRequestService.getAllUserItemRequests(requestor.getId());
        assertFalse(userItemRequests.isEmpty());
        assertEquals(1, userItemRequests.size());
        assertThat(itemRequest).usingRecursiveComparison().ignoringFields("created").isEqualTo(userItemRequests.get(0));
    }

    @Test
    public void getAllUserItemRequests_whenNoRequests_thenReturnItemRequests() {
        assertEquals(0, itemRequestRepository.count());

        val userItemRequests = itemRequestService.getAllUserItemRequests(requestor.getId());
        assertTrue(userItemRequests.isEmpty());
    }

    @Test
    public void getAllUserItemRequests_whenOnlyOtherUserRequests_thenReturnItemRequests() {
        assertEquals(0, itemRequestRepository.count());

        val itemRequestCreateDto = ItemRequestCreateDto.builder()
                .description("need item")
                .build();

        val itemRequest = itemRequestService.createAndGet(itemRequestCreateDto, otherRequestor.getId());
        assertEquals(1, itemRequestRepository.count());

        val userItemRequests = itemRequestService.getAllUserItemRequests(requestor.getId());
        assertTrue(userItemRequests.isEmpty());
    }

    @Test
    public void getAllUserItemRequests_whenManyItemRequest_thenReturnItemRequests() {
        val itemRequestList = new ArrayList<>();
        val itemRequestCount = 3;
        for (int counter = 1; counter <= itemRequestCount; counter++) {
            val itemRequestCreateDto = ItemRequestCreateDto.builder()
                    .description("need item " + counter)
                    .build();

            val itemRequest = itemRequestService.createAndGet(itemRequestCreateDto, requestor.getId());
            itemRequestList.add(itemRequest);
        }

        val userItemRequests = itemRequestService.getAllUserItemRequests(requestor.getId());
        assertFalse(userItemRequests.isEmpty());
        assertEquals(itemRequestList.size(), userItemRequests.size());
        assertThat(itemRequestList).usingRecursiveComparison()
                .ignoringFields("created")
                .ignoringCollectionOrder()
                .isEqualTo(userItemRequests);
        assertThat(userItemRequests).isSortedAccordingTo(itemRequestCreatedDescComparator);
    }

    @Test
    public void getAllUserItemRequests_whenManyItemRequestAndHasItems_thenReturnItemRequests() {
        final List<ItemRequestDto> itemRequestDtoList = new ArrayList<>();
        val itemRequestCount = 3;
        for (int counter = 1; counter <= itemRequestCount; counter++) {
            val itemRequestCreateDto = ItemRequestCreateDto.builder()
                    .description("need item " + counter)
                    .build();

            val itemRequest = itemRequestService.createAndGet(itemRequestCreateDto, requestor.getId());
            itemRequestDtoList.add(itemRequest);
        }

        var owner = User.builder()
                .name("owner")
                .email("owner@email.com")
                .build();
        owner = userRepository.save(owner);

        for (val itemRequestDto : itemRequestDtoList) {
            val itemRequest = itemRequestRepository.findById(itemRequestDto.getId()).get();
            val item = Item.builder()
                    .owner(owner)
                    .name("item")
                    .description("desc")
                    .isAvailable(true)
                    .request(itemRequest)
                    .build();
            itemRepository.save(item);
        }

        val userItemRequests = itemRequestService.getAllUserItemRequests(requestor.getId());
        assertFalse(userItemRequests.isEmpty());
        assertEquals(itemRequestDtoList.size(), userItemRequests.size());
        assertThat(itemRequestDtoList).usingRecursiveComparison()
                .ignoringFields("created")
                .ignoringFields("itemsByRequest")
                .ignoringCollectionOrder()
                .isEqualTo(userItemRequests);
        assertThat(userItemRequests).isSortedAccordingTo(itemRequestCreatedDescComparator);
        assertThat(userItemRequests).allMatch(r -> !r.getItemsByRequest().isEmpty());
        assertThat(userItemRequests).allMatch(r -> r.getItemsByRequest().size() == 1);
    }

    @AfterEach
    public void clean() {
        itemRequestRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }
}
