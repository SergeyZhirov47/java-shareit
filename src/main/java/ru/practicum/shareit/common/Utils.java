package ru.practicum.shareit.common;

import org.springframework.data.domain.Pageable;

import static java.util.Objects.nonNull;

public class Utils {
    public static Pageable createOffsetBasedPageRequest(Integer from, Integer size) {
        Pageable pageable = null;
        if (nonNull(from) && nonNull(size)) {
            pageable = new OffsetBasedPageRequest(from, size);
        }

        return pageable;
    }
}
