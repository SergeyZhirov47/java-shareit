package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // уникальный идентификатор бронирования
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item; // вещь, которую пользователь бронирует
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User booker; // пользователь, который осуществляет бронирование
    @Column(name = "start_booking")
    private LocalDateTime start; // дата и время начала бронирования
    @Column(name = "end_booking")
    private LocalDateTime end; // дата и время конца бронирования
    @Enumerated(EnumType.STRING)
    private BookingStatus status; // статус бронирования
}
