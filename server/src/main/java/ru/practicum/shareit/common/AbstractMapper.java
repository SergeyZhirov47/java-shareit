package ru.practicum.shareit.common;

import static java.util.Objects.isNull;

public abstract class AbstractMapper {
    public static <T> T getChanged(T original, T changed, boolean changedNullable) {
        if (isNull(changed) && !changedNullable) {
            return original;
        }

        if (isNull(original) || !original.equals(changed)) {
            return changed;
        }

        return original;
    }

    public static <T> T getChanged(T original, T changed) {
        return getChanged(original, changed, false);
    }
}
