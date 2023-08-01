package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
@Jacksonized
@Data
@AllArgsConstructor
public class ItemCreateDto {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    @JsonProperty("available")
    private Boolean isAvailable;
    private Long requestId;
}
