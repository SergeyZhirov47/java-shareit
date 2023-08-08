package ru.practicum.shareit.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.repository.CommentRepository;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommentRepositoryTest {
    private final CommentRepository commentRepository;
    private TestEntityManager em;

    @Test
    public void findByItemId() {

    }

    @Test
    public void findByItemIdIn() {

    }

    @Test
    public void findByItemIdInWithSort() {

    }
}
