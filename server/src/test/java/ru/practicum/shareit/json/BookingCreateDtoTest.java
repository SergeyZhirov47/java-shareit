package ru.practicum.shareit.json;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.common.ConstantParamStorage.INCOMING_DATE_FORMAT;

@JsonTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingCreateDtoTest {
    private final JacksonTester<BookingCreateDto> jacksonTester;

    @SneakyThrows
    @Test
    public void testSerialize() {
        final LocalDateTime start = LocalDateTime.now().plusDays(1);
        final LocalDateTime end = start.plusDays(2).plusMinutes(10);

        final BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        final String startStr = start.format(DateTimeFormatter.ofPattern(INCOMING_DATE_FORMAT));
        final String endStr = end.format(DateTimeFormatter.ofPattern(INCOMING_DATE_FORMAT));

        final String currentJson = jacksonTester.write(bookingCreateDto).getJson();
        final String expectedJson = String.format("{\"itemId\":%d," +
                        "\"start\":\"%s\"," +
                        "\"end\":\"%s\"}",
                1, startStr, endStr);

        assertThat(currentJson).isEqualTo(expectedJson);
    }

    @SneakyThrows
    @Test
    public void testDeserialize() {
        final String incomingJson = "{" +
                "\"itemId\":999," +
                "\"start\":\"2023-11-10T07:08:09\"," +
                "\"end\":\"2023-11-21T07:08:09\"" +
                "}";

        final BookingCreateDto bookingCreateDto = jacksonTester.parseObject(incomingJson);

        assertThat(bookingCreateDto.getItemId()).isEqualTo(999L);
        assertThat(bookingCreateDto.getStart()).isEqualTo(LocalDateTime.of(2023, 11, 10, 7, 8, 9));
        assertThat(bookingCreateDto.getEnd()).isEqualTo(LocalDateTime.of(2023, 11, 21, 7, 8, 9));
    }
}
