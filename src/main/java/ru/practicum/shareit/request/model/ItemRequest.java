package ru.practicum.shareit.request.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "requests")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //  уникальный идентификатор запроса
    private String description; // текст запроса, содержащий описание требуемой вещи
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User requestor; //  пользователь, создавший запрос
    private LocalDateTime created; // дата и время создания запроса
    @OneToMany(mappedBy = "request")
    private List<Item> itemsByRequest; // все предметы добавленные по данному запросу.
}
