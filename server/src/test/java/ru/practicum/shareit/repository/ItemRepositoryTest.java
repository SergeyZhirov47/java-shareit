package ru.practicum.shareit.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.DaoItem;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.DaoUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.common.Utils.createOffsetBasedPageRequest;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemRepositoryTest {
    private final DaoItem daoItem;
    private final DaoUser daoUser;
    private final ItemRequestRepository itemRequestRepository;
    private TestEntityManager em;

    private User owner;

    @BeforeEach
    public void createOwner() {
        owner = User.builder()
                .name("owner")
                .email("owner@somemail.com")
                .build();

        owner = daoUser.save(owner);
    }

    @Test
    public void findByOwnerId_whenOneItem_thenReturnOne() {
        final Item item = createAndGetItem(owner);

        final List<Item> ownerItems = daoItem.findByOwnerId(owner.getId());

        assertFalse(ownerItems.isEmpty());
        assertEquals(1, ownerItems.size());
        assertEquals(item, ownerItems.get(0));
    }

    @Test
    public void findByOwnerId_whenUserHasNoItem_thenReturnEmptyList() {
        final List<Item> allItems = daoItem.findAll();
        assertTrue(allItems.isEmpty());

        final List<Item> ownerItems = daoItem.findByOwnerId(owner.getId());
        assertTrue(ownerItems.isEmpty());
    }

    @Test
    public void findByOwnerId_whenOwnerHasManyItems_thenReturnMany() {
        final List<Item> items = new ArrayList<>();
        final int itemCount = 3;
        for (int counter = 1; counter <= itemCount; counter++) {
            Item item = Item.builder()
                    .owner(owner)
                    .name("item " + counter)
                    .description("Description for item " + counter)
                    .isAvailable(true)
                    .build();
            item = daoItem.save(item);
            items.add(item);
        }

        final List<Item> ownerItems = daoItem.findByOwnerId(owner.getId());
        assertFalse(ownerItems.isEmpty());
        assertEquals(items.size(), ownerItems.size());
        assertEquals(items, ownerItems);
    }

    @Test
    void findByOwnerIdWithUnpaged_whenOneItem_thenReturnOne() {
        final Item item = createAndGetItem(owner);

        final List<Item> ownerItems = daoItem.findByOwnerId(owner.getId(), Pageable.unpaged());

        assertFalse(ownerItems.isEmpty());
        assertEquals(1, ownerItems.size());
        assertEquals(item, ownerItems.get(0));
    }

    @Test
    void findByOwnerIdWithPageable_whenOneItem_thenReturnOne() {
        final Item item = createAndGetItem(owner);

        final List<Item> ownerItems = daoItem.findByOwnerId(owner.getId(), createOffsetBasedPageRequest(0, 10));

        assertFalse(ownerItems.isEmpty());
        assertEquals(1, ownerItems.size());
        assertEquals(item, ownerItems.get(0));
    }

    @Test
    public void findByOwnerIdWithPageable_whenUserHasNoItem_thenReturnEmptyList() {
        final List<Item> allItems = daoItem.findAll();
        assertTrue(allItems.isEmpty());

        final List<Item> ownerItems = daoItem.findByOwnerId(owner.getId(), createOffsetBasedPageRequest(0, 10));
        assertTrue(ownerItems.isEmpty());
    }

    @Test
    public void findByOwnerIdWithUnpaged_whenOwnerHasManyItems_thenReturnMany() {
        final List<Item> items = new ArrayList<>();
        final int itemCount = 3;
        for (int counter = 1; counter <= itemCount; counter++) {
            Item item = Item.builder()
                    .owner(owner)
                    .name("item " + counter)
                    .description("Description for item " + counter)
                    .isAvailable(true)
                    .build();
            item = daoItem.save(item);
            items.add(item);
        }

        final List<Item> ownerItems = daoItem.findByOwnerId(owner.getId(), Pageable.unpaged());
        assertFalse(ownerItems.isEmpty());
        assertEquals(items.size(), ownerItems.size());
        assertEquals(items, ownerItems);
    }

    @Test
    public void findByOwnerIdWithPageable_whenOwnerHasManyItems_thenReturnMany() {
        final List<Item> items = new ArrayList<>();
        final int itemCount = 3;
        for (int counter = 1; counter <= itemCount; counter++) {
            Item item = Item.builder()
                    .owner(owner)
                    .name("item " + counter)
                    .description("Description for item " + counter)
                    .isAvailable(true)
                    .build();
            item = daoItem.save(item);
            items.add(item);
        }

        final int size = 2;
        final List<Item> ownerItems = daoItem.findByOwnerId(owner.getId(), createOffsetBasedPageRequest(0, size));
        assertFalse(ownerItems.isEmpty());
        assertEquals(size, ownerItems.size());
        assertThat(ownerItems).containsAnyElementsOf(items);
    }

    @Test
    public void existsByIdAndOwnerId_whenExists_thenReturnTrue() {
        final Item item = createAndGetItem(owner);

        assertTrue(daoItem.existsByIdAndOwnerId(item.getId(), owner.getId()));
    }

    @Test
    public void existsByIdAndOwnerId_whenItemExistsByNotOwner_thenReturnFalse() {
        User notOwnerUser = User.builder()
                .name("some user")
                .email("email@someemail.com")
                .build();
        notOwnerUser = daoUser.save(notOwnerUser);

        final Item item = createAndGetItem(owner);

        assertTrue(daoItem.existsByIdAndOwnerId(item.getId(), owner.getId()));
        assertFalse(daoItem.existsByIdAndOwnerId(item.getId(), notOwnerUser.getId()));
    }

    @Test
    public void existsByIdAndOwnerId_whenUserNotExists_thenReturnFalse() {
        final Item item = createAndGetItem(owner);
        final long notExistedUserId = 9999L;

        assertFalse(daoUser.existsById(notExistedUserId));
        assertFalse(daoItem.existsByIdAndOwnerId(item.getId(), notExistedUserId));
    }

    @Test
    public void existsByIdAndOwnerId_whenItemNotExists_thenReturnFalse() {
        final long notExistedItemId = 9999L;

        assertFalse(daoItem.existsById(notExistedItemId));
        assertFalse(daoItem.existsByIdAndOwnerId(notExistedItemId, owner.getId()));
    }

    @Test
    public void getByIdAndOwnerId_whenExists_thenReturnItem() {
        final Item item = createAndGetItem(owner);

        final Item itemFromDao = daoItem.getByIdAndOwnerId(item.getId(), owner.getId());
        assertNotNull(itemFromDao);
        assertEquals(item.getOwner().getId(), itemFromDao.getOwner().getId());
        assertEquals(item, itemFromDao);
    }

    @Test
    public void getByIdAndOwnerId_whenItemExistsByNotOwner_thenThrowException() {
        User notOwnerUser = User.builder()
                .name("some user")
                .email("email@someemail.com")
                .build();
        notOwnerUser = daoUser.save(notOwnerUser);

        final Item item = createAndGetItem(owner);

        assertTrue(daoItem.existsByIdAndOwnerId(item.getId(), owner.getId()));

        final User finalNotOwnerUser = notOwnerUser;
        assertThrows(ItemNotFoundException.class, () -> daoItem.getByIdAndOwnerId(item.getId(), finalNotOwnerUser.getId()));
    }

    @Test
    public void getByIdAndOwnerId_whenUserNotExists_thenThrowException() {
        final Item item = createAndGetItem(owner);
        final long notExistedUserId = 9999L;

        assertFalse(daoUser.existsById(notExistedUserId));
        assertThrows(ItemNotFoundException.class, () -> daoItem.getByIdAndOwnerId(item.getId(), notExistedUserId));
    }

    @Test
    public void getByIdAndOwnerId_whenItemNotExists_thenThrowException() {
        final long notExistedItemId = 9999L;

        assertFalse(daoItem.existsById(notExistedItemId));
        assertThrows(ItemNotFoundException.class, () -> daoItem.getByIdAndOwnerId(notExistedItemId, owner.getId()));
    }

    @Test
    public void findAvailableByNameOrDescription_whenOneItemOk_thenReturnItem() {
        final String name = "Philosophers' stone";
        final String description = "Transmutate metals into gold!";

        Item item = Item.builder()
                .name(name)
                .description(description)
                .isAvailable(true)
                .owner(owner)
                .build();
        item = daoItem.save(item);

        final List<String> searchRequests = List.of(
                name,
                description,
                "stone",
                "gold",
                name.toUpperCase(),
                description.toUpperCase(),
                name.toLowerCase(),
                description.toLowerCase()
        );

        for (String search : searchRequests) {
            final List<Item> items = daoItem.findAvailableByNameOrDescription(search);

            assertFalse(items.isEmpty());
            assertEquals(item, items.get(0));
        }
    }

    @Test
    public void findAvailableByNameOrDescription_whenManyItemsOk_thenReturnItems() {
        final List<String> names = List.of("Electric saw", "Electric drill", "Electric jackhammer");
        final List<String> descriptions = List.of("big saw", "VERY BIG ", "Biggest in the world!");

        final List<Item> items = new ArrayList<>();
        for (int index = 0; index < names.size(); index++) {
            Item item = Item.builder()
                    .name(names.get(index))
                    .description(descriptions.get(index))
                    .isAvailable(true)
                    .owner(owner)
                    .build();

            item = daoItem.save(item);
            items.add(item);
        }

        final List<String> searchRequests = List.of(
                "Electric",
                "ElEcTriC",
                "Electric".toLowerCase(),
                "Electric".toUpperCase(),
                "big",
                "big".toLowerCase(),
                "big".toUpperCase(),
                "bIG"
        );

        for (String search : searchRequests) {
            final List<Item> itemsFromDb = daoItem.findAvailableByNameOrDescription(search);

            assertFalse(itemsFromDb.isEmpty());
            assertEquals(items, itemsFromDb);
        }
    }

    @Test
    public void findAvailableByNameOrDescription_whenNoItems_thenReturnEmpty() {
        assertTrue(daoItem.findAll().isEmpty());
        assertEquals(Collections.emptyList(), daoItem.findAvailableByNameOrDescription(""));
    }

    @Test
    public void findAvailableByNameOrDescription_whenNotFind_thenReturnEmpty() {
        final Item item = Item.builder()
                .name("Item")
                .description("some item")
                .isAvailable(true)
                .owner(owner)
                .build();
        daoItem.save(item);

        assertFalse(daoItem.findAll().isEmpty());
        assertFalse(daoItem.findAvailableByNameOrDescription("item").isEmpty());
        assertEquals(Collections.emptyList(), daoItem.findAvailableByNameOrDescription("search words what not contains in name or description"));
    }

    @Test
    public void findAvailableByNameOrDescription_whenDescriptionOkButNoAvailable_thenReturnEmpty() {
        final Item item = Item.builder()
                .name("Item")
                .description("some item")
                .isAvailable(false)
                .owner(owner)
                .build();
        daoItem.save(item);

        assertEquals(Collections.emptyList(), daoItem.findAvailableByNameOrDescription("item"));
    }

    @Test
    public void findAvailableByNameOrDescriptionWithUnpaged_whenOneItemOk_thenReturnItem() {
        final String name = "Philosophers' stone";
        final String description = "Transmutate metals into gold!";

        Item item = Item.builder()
                .name(name)
                .description(description)
                .isAvailable(true)
                .owner(owner)
                .build();
        item = daoItem.save(item);

        final String search = "stone";
        final List<Item> items = daoItem.findAvailableByNameOrDescription(search, Pageable.unpaged());

        assertFalse(items.isEmpty());
        assertEquals(item, items.get(0));
    }

    @Test
    public void findAvailableByNameOrDescriptionWithPageable_whenManyItemsOk_thenReturnItems() {
        final List<String> names = List.of("Electric saw", "Electric drill", "Electric jackhammer");
        final List<String> descriptions = List.of("big saw", "VERY BIG ", "Biggest in the world!");

        final List<Item> items = new ArrayList<>();
        for (int index = 0; index < names.size(); index++) {
            Item item = Item.builder()
                    .name(names.get(index))
                    .description(descriptions.get(index))
                    .isAvailable(true)
                    .owner(owner)
                    .build();

            item = daoItem.save(item);
            items.add(item);
        }

        final int size = 1;
        final String search = "Electric";
        final List<Item> itemsFromDb = daoItem.findAvailableByNameOrDescription(search, createOffsetBasedPageRequest(0, size));

        assertFalse(itemsFromDb.isEmpty());
        assertEquals(size, itemsFromDb.size());
        assertThat(itemsFromDb).containsAnyElementsOf(items);
    }

    @Test
    public void getItemById_thenExists_thenReturnItem() {
        final Item item = createAndGetItem(owner);
        final long itemId = item.getId();

        final Item itemFromDb = daoItem.getItemById(itemId);
        assertEquals(itemId, itemFromDb.getId());
        assertEquals(item, itemFromDb);
    }

    @Test
    public void getItemById_thenBotExists_thenThrowException() {
        final List<Item> items = daoItem.findAll();
        assertTrue(items.isEmpty());

        final long notExistedItemId = 9999L;
        assertThrows(ItemNotFoundException.class, () -> daoItem.getItemById(notExistedItemId));
    }

    @Test
    public void checkItemExists_whenExists_thenOk() {
        final Item item = createAndGetItem(owner);
        assertDoesNotThrow(() -> daoItem.checkItemExists(item.getId()));
    }

    @Test
    public void checkItemExists_whenNotExists_thenThrowException() {
        final List<Item> items = daoItem.findAll();
        assertTrue(items.isEmpty());

        final long notExistedItemId = 9999L;
        assertThrows(ItemNotFoundException.class, () -> daoItem.checkItemExists(notExistedItemId));
    }

    @AfterEach
    public void clean() {
        daoItem.deleteAll();
        daoUser.deleteAll();
    }

    private Item createAndGetItem(User owner) {
        Item item = Item.builder()
                .owner(owner)
                .name("Awesome item")
                .description("Description for awesome item")
                .isAvailable(true)
                .build();

        return daoItem.save(item);
    }

    @Nested
    public class TestFindItemsForItemRequestMethods {
        private User requestor;

        @BeforeEach
        public void init() {
            requestor = User.builder()
                    .name("requestor")
                    .email("requestor@email.com")
                    .build();
            requestor = daoUser.save(requestor);
        }

        @Test
        public void findItemsForItemRequest_whenOneItem_thenReturnItem() {
            ItemRequest itemRequest = ItemRequest.builder()
                    .requestor(requestor)
                    .description("need item")
                    .created(LocalDateTime.now())
                    .build();
            itemRequest = itemRequestRepository.save(itemRequest);

            Item item = Item.builder()
                    .owner(owner)
                    .name("Item")
                    .description("desc")
                    .isAvailable(true)
                    .request(itemRequest)
                    .build();
            item = daoItem.save(item);

            final List<Item> itemsForRequest = daoItem.findItemsForItemRequest(itemRequest.getId());
            assertFalse(itemsForRequest.isEmpty());
            assertEquals(1, itemsForRequest.size());
            assertEquals(item, itemsForRequest.get(0));
        }

        @Test
        public void findItemsForItemRequest_whenManyItems_thenReturnItems() {
            ItemRequest itemRequest = ItemRequest.builder()
                    .requestor(requestor)
                    .description("need item")
                    .created(LocalDateTime.now())
                    .build();
            itemRequest = itemRequestRepository.save(itemRequest);

            final int itemCount = 3;
            final List<Item> exceptedResult = new ArrayList<>();
            for (int counter = 1; counter <= itemCount; counter++) {
                Item item = Item.builder()
                        .owner(owner)
                        .name("Item " + counter)
                        .description("desc " + counter)
                        .isAvailable(true)
                        .request(itemRequest)
                        .build();
                item = daoItem.save(item);

                exceptedResult.add(item);
            }

            final List<Item> itemsForRequest = daoItem.findItemsForItemRequest(itemRequest.getId());
            assertFalse(itemsForRequest.isEmpty());
            assertEquals(exceptedResult.size(), itemsForRequest.size());
            assertEquals(exceptedResult, itemsForRequest);
        }

        @Test
        public void findItemsForItemRequest_whenNoItems_thenReturnEmpty() {
            ItemRequest itemRequest = ItemRequest.builder()
                    .requestor(requestor)
                    .description("need item")
                    .created(LocalDateTime.now())
                    .build();
            itemRequest = itemRequestRepository.save(itemRequest);

            final List<Item> itemsForRequest = daoItem.findItemsForItemRequest(itemRequest.getId());
            assertTrue(itemsForRequest.isEmpty());
        }

        @Test
        public void findItemsForItemRequests_whenOneItem_thenReturnItem() {
            ItemRequest itemRequest = ItemRequest.builder()
                    .requestor(requestor)
                    .description("need item")
                    .created(LocalDateTime.now())
                    .build();
            itemRequest = itemRequestRepository.save(itemRequest);
            final long itemRequestId = itemRequest.getId();

            Item item = Item.builder()
                    .owner(owner)
                    .name("Item")
                    .description("desc")
                    .isAvailable(true)
                    .request(itemRequest)
                    .build();
            item = daoItem.save(item);

            final List<Long> itemRequestIds = List.of(itemRequestId);
            final Map<Long, List<Item>> itemsForRequest = daoItem.findItemsForItemRequests(itemRequestIds);
            assertFalse(itemsForRequest.isEmpty());
            assertTrue(itemsForRequest.containsKey(itemRequestId));
            assertEquals(item, itemsForRequest.get(itemRequestId).get(0));
        }

        @Test
        public void findItemsForItemRequests_whenManyItems_thenReturnItems() {
            final int itemRequestCount = 2;
            final List<ItemRequest> requests = new ArrayList<>();
            for (int counter = 1; counter <= itemRequestCount; counter++) {
                ItemRequest itemRequest = ItemRequest.builder()
                        .requestor(requestor)
                        .description("need item " + counter)
                        .created(LocalDateTime.now())
                        .build();
                itemRequest = itemRequestRepository.save(itemRequest);

                requests.add(itemRequest);
            }

            final int itemCount = 3;
            final List<Item> exceptedResult = new ArrayList<>();
            for (ItemRequest itemRequest : requests) {
                for (int counter = 1; counter <= itemCount; counter++) {
                    Item item = Item.builder()
                            .owner(owner)
                            .name("Item " + counter)
                            .description("desc " + counter)
                            .isAvailable(true)
                            .request(itemRequest)
                            .build();
                    item = daoItem.save(item);

                    exceptedResult.add(item);
                }
            }

            final List<Long> itemRequestIds = requests.stream().map(ItemRequest::getId).collect(toUnmodifiableList());
            final Map<Long, List<Item>> itemsForRequest = daoItem.findItemsForItemRequests(itemRequestIds);
            final List<Item> flatItems = itemsForRequest.values().stream().flatMap(List::stream).collect(toUnmodifiableList());

            assertFalse(itemsForRequest.isEmpty());
            assertEquals(itemRequestIds.size(), itemsForRequest.size());
            assertThat(itemsForRequest).containsKeys(itemRequestIds.toArray(Long[]::new));
            assertEquals(exceptedResult, flatItems);
        }

        @Test
        public void findItemsForItemRequests_whenManyItemsButForOnlyOneRequest_thenReturnItems() {
            ItemRequest targetItemRequest = ItemRequest.builder()
                    .requestor(requestor)
                    .description("need item 1")
                    .created(LocalDateTime.now())
                    .build();
            targetItemRequest = itemRequestRepository.save(targetItemRequest);

            ItemRequest itemRequest2 = ItemRequest.builder()
                    .requestor(requestor)
                    .description("need item 2")
                    .created(LocalDateTime.now())
                    .build();
            itemRequest2 = itemRequestRepository.save(itemRequest2);

            final int itemCount = 3;
            final List<Item> exceptedResult = new ArrayList<>();
            for (int counter = 1; counter <= itemCount; counter++) {
                Item item = Item.builder()
                        .owner(owner)
                        .name("Item " + counter)
                        .description("desc " + counter)
                        .isAvailable(true)
                        .request(targetItemRequest)
                        .build();
                item = daoItem.save(item);

                exceptedResult.add(item);
            }
            for (int counter = 1; counter <= itemCount; counter++) {
                Item item = Item.builder()
                        .owner(owner)
                        .name("Item " + counter)
                        .description("desc " + counter)
                        .isAvailable(true)
                        .request(itemRequest2)
                        .build();
                item = daoItem.save(item);
            }

            final List<Long> itemRequestIds = List.of(targetItemRequest.getId());
            final Map<Long, List<Item>> itemsForRequest = daoItem.findItemsForItemRequests(itemRequestIds);
            final List<Item> flatItems = itemsForRequest.values().stream().flatMap(List::stream).collect(toUnmodifiableList());

            assertFalse(itemsForRequest.isEmpty());
            assertEquals(itemRequestIds.size(), itemsForRequest.size());
            assertThat(itemsForRequest).containsKeys(itemRequestIds.toArray(Long[]::new));
            assertEquals(exceptedResult, flatItems);
        }

        @Test
        public void findItemsForItemRequests_whenNoItems_thenReturnEmpty() {
            ItemRequest itemRequest = ItemRequest.builder()
                    .requestor(requestor)
                    .description("need item")
                    .created(LocalDateTime.now())
                    .build();
            itemRequest = itemRequestRepository.save(itemRequest);

            ItemRequest itemRequest2 = ItemRequest.builder()
                    .requestor(requestor)
                    .description("need item 2")
                    .created(LocalDateTime.now())
                    .build();
            itemRequest2 = itemRequestRepository.save(itemRequest2);

            final List<Long> itemRequestIds = List.of(itemRequest.getId(), itemRequest2.getId());
            final Map<Long, List<Item>> itemsForRequest = daoItem.findItemsForItemRequests(itemRequestIds);
            assertTrue(itemsForRequest.isEmpty());
        }

        @AfterEach
        public void clean() {
            itemRequestRepository.deleteAll();
            daoItem.deleteAll();
        }
    }
}
