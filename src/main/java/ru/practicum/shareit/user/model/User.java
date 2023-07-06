package ru.practicum.shareit.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * TODO Sprint add-controllers.
 */
@Builder
@Data
@AllArgsConstructor
public class User {
    private Long id;  // уникальный идентификатор пользователя
    @NotBlank
    private String name; // имя или логин пользователя
    @NotBlank
    @Email
    private String email; // адрес электронной почты. Уникален для каждого пользователя.
}
