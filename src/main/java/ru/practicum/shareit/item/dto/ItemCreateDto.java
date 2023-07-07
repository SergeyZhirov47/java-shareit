package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Builder
@Jacksonized
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ItemCreateDto {
    @NotEmpty
    private String name;
    @NotEmpty
    private String description;
    @NotNull
    @JsonProperty("available")
    private Boolean isAvailable;
}
