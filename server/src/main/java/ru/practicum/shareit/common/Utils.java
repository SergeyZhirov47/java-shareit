package ru.practicum.shareit.common;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static java.util.Objects.nonNull;

public class Utils {
    public static Pageable createOffsetBasedPageRequest(Integer from, Integer size, Sort sort) {
        Pageable pageable = null;
        if (nonNull(from) && nonNull(size)) {
            pageable = new OffsetBasedPageRequest(from, size, sort);
        }

        return pageable;
    }

    public static Pageable createOffsetBasedPageRequest(Integer from, Integer size) {
        return createOffsetBasedPageRequest(from, size, Sort.unsorted());
    }
}
