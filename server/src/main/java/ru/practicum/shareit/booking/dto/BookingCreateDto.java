package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static ru.practicum.shareit.common.ConstantParamStorage.INCOMING_DATE_FORMAT;

@Builder
@Jacksonized
@Data
@AllArgsConstructor
public class BookingCreateDto {
    @NotNull
    private long itemId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = INCOMING_DATE_FORMAT)
    @NotNull
    @FutureOrPresent
    private LocalDateTime start;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = INCOMING_DATE_FORMAT)
    @NotNull
    @FutureOrPresent
    private LocalDateTime end;
}
