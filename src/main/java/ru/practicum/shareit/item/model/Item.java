package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;

@Entity
@Table(name = "items")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // уникальный идентификатор вещи
    private String name; // краткое название
    private String description; // развёрнутое описание
    @Column(name = "is_available")
    private boolean isAvailable; // статус о том, доступна или нет вещь для аренды
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner; // владелец вещи
    // если вещь была создана по запросу другого пользователя,
    // то в этом поле будет храниться ссылка на соответствующий запрос
    @Transient
    private ItemRequest request;
}
