package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;

@Builder
@Jacksonized
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class CommentCreateDto {
    @NotBlank
    private String text;
}
