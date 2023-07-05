package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
public class User {
    private Long id;  // уникальный идентификатор пользователя
    private String name; // имя или логин пользователя
    @Email
    private String email; // адрес электронной почты. Уникален для каждого пользователя.
}
