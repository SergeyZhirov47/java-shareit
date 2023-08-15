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
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.DaoItem;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.DaoUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommentRepositoryTest {
    private final CommentRepository commentRepository;
    private final DaoUser daoUser;
    private final DaoItem daoItem;
    private TestEntityManager em;

    private User commentator;

    @BeforeEach
    public void init() {
        commentator = User.builder()
                .name("commentator")
                .email("iamcommentator@randomemail.com")
                .build();
        daoUser.save(commentator);
    }

    @Nested
    class TestFindByItemId {
        private User owner;
        private Item item;

        @BeforeEach
        public void init() {
            owner = User.builder()
                    .name("item owner")
                    .email("mail@randomemail.com")
                    .build();
            daoUser.save(owner);

            item = Item.builder()
                    .owner(owner)
                    .name("Item for comments")
                    .description("for test")
                    .isAvailable(true)
                    .build();
            daoItem.save(item);
        }

        @Test
        public void findByItemId_whenItemExists_thenReturnComments() {
            final List<Comment> comments = new ArrayList<>();
            final int commentsCount = 3;

            for (int counter = 1; counter <= commentsCount; counter++) {
                Comment comment = Comment.builder()
                        .item(item)
                        .author(commentator)
                        .created(LocalDateTime.now())
                        .text("some comment text " + counter)
                        .build();
                comment = commentRepository.save(comment);
                comments.add(comment);
            }

            final List<Comment> commentsFromDB = commentRepository.findByItemId(item.getId());
            assertFalse(commentsFromDB.isEmpty());
            assertEquals(comments.size(), commentsFromDB.size());
            assertEquals(comments, commentsFromDB);
        }

        @Test
        public void findByItemId_whenItemNotExists_thenReturnEmpty() {
            final long itemNotExistedId = 9999L;
            assertFalse(daoItem.existsById(itemNotExistedId));

            final List<Comment> commentsFromDB = commentRepository.findByItemId(item.getId());
            assertTrue(commentsFromDB.isEmpty());
        }

        @Test
        public void findByItemId_whenNoComments_thenReturnEmpty() {
            final List<Comment> commentsFromDB = commentRepository.findByItemId(item.getId());
            assertTrue(commentsFromDB.isEmpty());
        }

        @AfterEach
        public void clean() {
            commentRepository.deleteAll();
            daoItem.deleteAll();
            daoUser.deleteAll();
        }
    }

    @Nested
    class TestFindByItemIdIn {
        private User owner1;
        private User owner2;
        private List<Item> owner1Items;
        private List<Item> owner2Items;

        @BeforeEach
        public void init() {
            owner1 = User.builder()
                    .name("item owner1")
                    .email("owner1@randomemail.com")
                    .build();
            daoUser.save(owner1);

            owner2 = User.builder()
                    .name("item owner2")
                    .email("owner2@randomemail.com")
                    .build();
            daoUser.save(owner2);

            owner1Items = new ArrayList<>();
            owner2Items = new ArrayList<>();

            final int owner1ItemsCount = 1;
            for (int count = 1; count <= owner1ItemsCount; count++) {
                Item item = Item.builder()
                        .owner(owner1)
                        .name("owner1 Item " + count)
                        .description("item owner1 for test")
                        .isAvailable(true)
                        .build();
                daoItem.save(item);

                owner1Items.add(item);
            }

            final int owner2ItemsCount = 3;
            for (int count = 1; count <= owner2ItemsCount; count++) {
                Item item = Item.builder()
                        .owner(owner2)
                        .name("owner2 Item " + count)
                        .description("item owner2 for test")
                        .isAvailable(true)
                        .build();
                daoItem.save(item);

                owner2Items.add(item);
            }
        }

        @Test
        public void findByItemIdIn_whenItemsExists_thenReturnComments() {
            final List<Comment> comments = new ArrayList<>();
            final int commentsCount = 3;

            for (int counter = 1; counter <= commentsCount; counter++) {
                for (Item item : owner1Items) {
                    Comment comment = Comment.builder()
                            .item(item)
                            .author(commentator)
                            .created(LocalDateTime.now())
                            .text("some comment text " + counter)
                            .build();
                    comment = commentRepository.save(comment);
                    comments.add(comment);
                }
            }

            final List<Long> itemIds = owner1Items.stream().map(Item::getId).collect(Collectors.toList());
            final List<Comment> commentsFromRepo = commentRepository.findByItemIdIn(itemIds);

            assertFalse(commentsFromRepo.isEmpty());
            assertEquals(comments.size(), commentsFromRepo.size());
            assertEquals(comments, commentsFromRepo);
        }

        @Test
        public void findByItemIdIn_whenItemsFromDiffrentOwner_thenReturnComments() {
            final List<Comment> comments = new ArrayList<>();
            final int commentsCount = 3;

            final List<Item> itemsForTest = List.of(owner1Items.get(0), owner2Items.get(0));

            for (int counter = 1; counter <= commentsCount; counter++) {
                for (Item item : itemsForTest) {
                    Comment comment = Comment.builder()
                            .item(item)
                            .author(commentator)
                            .created(LocalDateTime.now())
                            .text("some comment text " + counter)
                            .build();
                    comment = commentRepository.save(comment);
                    comments.add(comment);
                }
            }

            final List<Long> itemIds = itemsForTest.stream().map(Item::getId).collect(Collectors.toList());
            final List<Comment> commentsFromRepo = commentRepository.findByItemIdIn(itemIds);

            assertFalse(commentsFromRepo.isEmpty());
            assertEquals(comments.size(), commentsFromRepo.size());
            assertEquals(comments, commentsFromRepo);
        }

        @Test
        public void findByItemIdIn_whenNoComments_thenReturnEmpty() {
            final List<Long> itemIds = owner1Items.stream().map(Item::getId).collect(Collectors.toList());

            final List<Comment> commentsFromRepo = commentRepository.findByItemIdIn(itemIds);
            assertTrue(commentsFromRepo.isEmpty());
        }

        @Test
        public void findByItemIdIn_whenEmptyIdList_thenReturnEmpty() {
            final List<Comment> commentsFromRepo = commentRepository.findByItemIdIn(Collections.emptyList());
            assertTrue(commentsFromRepo.isEmpty());
        }

        @Test
        public void findByItemIdIn_whenHasNotExistsItemId_thenReturnComments() {
            final List<Comment> comments = new ArrayList<>();
            final int commentsCount = 3;

            for (int counter = 1; counter <= commentsCount; counter++) {
                for (Item item : owner1Items) {
                    Comment comment = Comment.builder()
                            .item(item)
                            .author(commentator)
                            .created(LocalDateTime.now())
                            .text("some comment text " + counter)
                            .build();
                    comment = commentRepository.save(comment);
                    comments.add(comment);
                }
            }

            final List<Long> itemIds = owner1Items.stream().map(Item::getId).collect(Collectors.toList());
            final List<Long> notExistedItemIds = List.of(9999L, 10000L, 11111L);
            for (Long id : notExistedItemIds) {
                assertFalse(daoItem.existsById(id));
            }
            itemIds.addAll(notExistedItemIds);

            final List<Comment> commentsFromRepo = commentRepository.findByItemIdIn(itemIds);

            assertFalse(commentsFromRepo.isEmpty());
            assertEquals(comments.size(), commentsFromRepo.size());
            assertEquals(comments, commentsFromRepo);
        }

        @AfterEach
        public void clean() {
            commentRepository.deleteAll();
            daoItem.deleteAll();
            daoUser.deleteAll();
        }
    }

    @AfterEach
    public void clean() {
        commentRepository.deleteAll();
        daoItem.deleteAll();
        daoUser.deleteAll();
    }
}
