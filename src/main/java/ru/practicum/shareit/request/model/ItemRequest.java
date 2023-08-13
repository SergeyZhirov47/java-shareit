package ru.practicum.shareit.request.model;

import lombok.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "requests")
@Builder
@Getter
@Setter
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
