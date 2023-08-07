package ru.practicum.shareit.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;

// @DataJpaTest
// @SpringBootTest
@DataJpaTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemRequestRepositoryTest {
    private final ItemRequestRepository itemRequestRepository;
    private TestEntityManager em;

    @BeforeEach
    public void addItemRequest() {

    }

    @Test
    public void findByRequestorId() {

    }

    @Test
    public void findByRequestorIdNot() {

    }

    @AfterEach
    public void deleteAllItemRequests() {
        itemRequestRepository.deleteAll();
    }
}
