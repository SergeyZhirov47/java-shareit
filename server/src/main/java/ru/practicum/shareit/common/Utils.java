package ru.practicum.shareit.common;

import org.springframework.data.domain.Pageable;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class Utils {
    public static Pageable createOffsetBasedPageRequest(Integer from, Integer size) {
        Pageable pageable = null;
        if (nonNull(from) && nonNull(size)) {
            pageable = new OffsetBasedPageRequest(from, size);
        }

        return pageable;
    }

    public static void validatePageableParams(Integer from, Integer size) {
        if (isNull(from) && isNull(size)) {
            return;
        }

        if ((isNull(from) && nonNull(size)) || (nonNull(from) && isNull(size))) {
            throw new ValidationException("Параметры from и size должны быть оба указаны");
        }

        if (from == 0 && size == 0) {
            throw new ValidationException("Оба параметра не могут быть равны нулю");
        }
        if (from < 0) {
            throw new ValidationException("Параметр from не может быть меньше нуля");
        }
        if (size <= 0) {
            throw new ValidationException("Параметр size не может быть меньше или равным нулю");
        }
    }
}
