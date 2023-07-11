package ru.practicum.shareit.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class IdGenerator {
    @Getter
    private Long lastId;

    public Long getNext() {
        lastId++;
        return lastId;
    }
}
