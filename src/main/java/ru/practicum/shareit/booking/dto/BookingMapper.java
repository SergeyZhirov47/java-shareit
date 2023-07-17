package ru.practicum.shareit.booking.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.common.AbstractMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Optional;


@UtilityClass
public class BookingMapper extends AbstractMapper {
    private ItemService itemService;
    private ItemRepository itemRepository;

    public BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(booking.getItem())
                .build();
    }

    public Booking toBooking(BookingCreateDto bookingCreateDto) {
        // ToDo
        // как передать ссылку на предмет, если известен только id
        // ToDo
        // Сервис возвращает только DTO. не удобно.
        // два метода нужно? Или контроллер пускай сам конвертирует в dto?
        // или использовать репозиторий
       //final ItemDto itemDto = itemService.getById(bookingCreateDto.getItemId());
       // ItemMapper.toItem() тут тогда еще и пользователь нужен

        // если через репозиторий, то опять проверки. Не понимаю как со всем этим правильно работать.
        final Optional<Item> itemOpt = itemRepository.findById(bookingCreateDto.getItemId());

        if (itemOpt.isEmpty()) {
            return null;
        }
        final Item item = itemOpt.get();

        return Booking.builder()
                .start(bookingCreateDto.getStart())
                .end(bookingCreateDto.getEnd())
                .item(item)
                .build();
    }

    public Booking updateIfDifferent(final Booking booking, final BookingDto bookingWithChanges) {
        return Booking.builder()

                .build();
    }
}
