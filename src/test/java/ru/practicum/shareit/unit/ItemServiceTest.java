package ru.practicum.shareit.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.repository.DaoItem;
import ru.practicum.shareit.item.service.ItemServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    private DaoItem itemRepository;
    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    public void test1() {

    }
}
