package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    //  Добавление нового запроса на бронирование.
    //  Запрос может быть создан любым пользователем, а затем подтверждён владельцем вещи.
    //  После создания запрос находится в статусе WAITING — «ожидает подтверждения».
    @Override
    public BookingDto create(BookingCreateDto newBooking) {
        // Проверяем, что пользователь существует.
        // Проверяем, что вещь существует.
        // Проверяем, что вещь доступна.


        return null;
    }
}
