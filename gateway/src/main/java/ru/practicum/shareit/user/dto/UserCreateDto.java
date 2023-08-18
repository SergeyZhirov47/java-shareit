package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Builder
@Jacksonized
@Data
@AllArgsConstructor
public class UserCreateDto {
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
}
