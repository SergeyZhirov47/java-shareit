package ru.practicum.shareit.common;

public class IdGenerator {
    private Long lastId;

    public IdGenerator() {
        lastId = 0L;
    }

    public IdGenerator(Long startId) {
        this.lastId = startId;
    }

    public Long getNext() {
        lastId++;
        return lastId;
    }

    public Long getCurrent() {
        return lastId;
    }
}
