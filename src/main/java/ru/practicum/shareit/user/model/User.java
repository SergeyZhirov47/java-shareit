package ru.practicum.shareit.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "users")
@Builder
@Data
@AllArgsConstructor
public class User {
    @Id
    private Long id;  // уникальный идентификатор пользователя
    @NotBlank
    private String name; // имя или логин пользователя
    @NotBlank
    @Email
    private String email; // адрес электронной почты. Уникален для каждого пользователя.
}
