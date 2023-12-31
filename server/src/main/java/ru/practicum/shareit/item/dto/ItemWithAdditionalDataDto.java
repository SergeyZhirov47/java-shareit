package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import ru.practicum.shareit.booking.dto.BookingForItemDto;

import java.util.List;

@Builder
@Jacksonized
@Data
@AllArgsConstructor
public class ItemWithAdditionalDataDto {
    private Long id;
    private String name;
    private String description;
    @JsonProperty("available")
    private Boolean isAvailable;
    private BookingForItemDto lastBooking;
    private BookingForItemDto nextBooking;
    private List<CommentDto> comments;
}
