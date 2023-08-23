package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

import static ru.practicum.shareit.common.ConstantParamStorage.INCOMING_DATE_FORMAT;

@Builder
@Jacksonized
@Data
@AllArgsConstructor
public class BookingCreateDto {
    private long itemId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = INCOMING_DATE_FORMAT)
    private LocalDateTime start;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = INCOMING_DATE_FORMAT)
    private LocalDateTime end;
}
