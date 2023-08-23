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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.DaoUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemRequestRepositoryTest {
    private final ItemRequestRepository itemRequestRepository;
    private final DaoUser daoUser;
    private TestEntityManager em;

    private User requestor1;
    private User requestor2;

    @BeforeEach
    public void init() {
        requestor1 = User.builder()
                .name("requestor1")
                .email("requestor1@email.com")
                .build();
        requestor1 = daoUser.save(requestor1);

        requestor2 = User.builder()
                .name("requestor2")
                .email("requestor2@email.com")
                .build();
        requestor2 = daoUser.save(requestor2);
    }

    @Test
    public void findByRequestorId_whenOneRequest_thenReturnRequest() {
        ItemRequest itemRequest = ItemRequest.builder()
                .description("description")
                .requestor(requestor1)
                .created(LocalDateTime.now())
                .build();
        itemRequest = itemRequestRepository.save(itemRequest);

        final List<ItemRequest> requestsFromRepo = itemRequestRepository.findByRequestorId(requestor1.getId());
        assertFalse(requestsFromRepo.isEmpty());
        assertEquals(1, requestsFromRepo.size());
        assertEquals(List.of(itemRequest), requestsFromRepo);
    }

    @Test
    public void findByRequestorId_whenManyRequest_thenReturnRequest() {
        final int requestCount = 3;
        final List<ItemRequest> requestList = new ArrayList<>();
        for (int count = 1; count <= requestCount; count++) {
            ItemRequest itemRequest = ItemRequest.builder()
                    .description("description " + count)
                    .requestor(requestor1)
                    .created(LocalDateTime.now())
                    .build();
            itemRequest = itemRequestRepository.save(itemRequest);

            requestList.add(itemRequest);
        }

        final List<ItemRequest> requestsFromRepo = itemRequestRepository.findByRequestorId(requestor1.getId());
        assertFalse(requestsFromRepo.isEmpty());
        assertEquals(requestList.size(), requestsFromRepo.size());
        assertEquals(requestList, requestsFromRepo);
    }

    @Test
    public void findByRequestorId_whenNoRequest_thenReturnEmpty() {
        final List<ItemRequest> requestsFromRepo = itemRequestRepository.findByRequestorId(requestor1.getId());
        assertTrue(requestsFromRepo.isEmpty());
    }

    @Test
    public void findByRequestorIdNot_whenHasOtherRequests_thenReturnRequests() {
        final int requestCount = 3;
        final List<ItemRequest> requestList = new ArrayList<>();
        for (int count = 1; count <= requestCount; count++) {
            ItemRequest itemRequest = ItemRequest.builder()
                    .description("description " + count)
                    .requestor(requestor1)
                    .created(LocalDateTime.now())
                    .build();
            itemRequest = itemRequestRepository.save(itemRequest);

            requestList.add(itemRequest);
        }

        final List<ItemRequest> requestsFromRepo = itemRequestRepository.findByRequestorIdNot(requestor2.getId(), Sort.unsorted());
        assertFalse(requestsFromRepo.isEmpty());
        assertEquals(requestList.size(), requestsFromRepo.size());
        assertEquals(requestList, requestsFromRepo);
    }

    @Test
    public void findByRequestorIdNot_whenTryFindYouRequests_thenReturnEmpty() {
        final int requestCount = 3;
        for (int count = 1; count <= requestCount; count++) {
            ItemRequest itemRequest = ItemRequest.builder()
                    .description("description " + count)
                    .requestor(requestor1)
                    .created(LocalDateTime.now())
                    .build();
            itemRequestRepository.save(itemRequest);
        }

        final List<ItemRequest> requestsFromRepo = itemRequestRepository.findByRequestorIdNot(requestor1.getId(), Sort.unsorted());
        assertTrue(requestsFromRepo.isEmpty());
    }

    @AfterEach
    public void deleteAllItemRequests() {
        itemRequestRepository.deleteAll();
        daoUser.deleteAll();
    }
}
