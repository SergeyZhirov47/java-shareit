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
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.DaoItem;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.DaoUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemRepositoryTest {
    private final DaoItem daoItem;
    private final DaoUser daoUser;
    private TestEntityManager em;

    private User owner;

    @BeforeEach
    public void createOwner() {
        owner = User.builder()
                .name("user")
                .email("some@somemail.com")
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

    // Те же самые тесты только с pageable...
    @Test
    public void findByOwnerIdWithPageable() {

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

    // те же тесты, что и exists ?
    @Test
    public void getByIdAndOwnerId() {

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
        Item item = Item.builder()
                .name("Item")
                .description("some item")
                .isAvailable(true)
                .owner(owner)
                .build();
        item = daoItem.save(item);

        assertFalse(daoItem.findAll().isEmpty());
        assertFalse(daoItem.findAvailableByNameOrDescription("item").isEmpty());
        assertEquals(Collections.emptyList(), daoItem.findAvailableByNameOrDescription("search words what not contains in name or description"));
    }

    @Test
    public void findAvailableByNameOrDescription_whenDescriptionOkButNoAvailable_thenReturnEmpty() {
        Item item = Item.builder()
                .name("Item")
                .description("some item")
                .isAvailable(false)
                .owner(owner)
                .build();
        item = daoItem.save(item);

        assertEquals(Collections.emptyList(), daoItem.findAvailableByNameOrDescription("item"));
    }

    // Те же самые тесты только с pageable...
    @Test
    public void findAvailableByNameOrDescriptionWithPageable() {

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
    public void deleteAllItems() {
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
}
