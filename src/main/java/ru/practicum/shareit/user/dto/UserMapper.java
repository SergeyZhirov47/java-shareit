package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.model.User;

import static java.util.Objects.isNull;

public class UserMapper {
    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .email(userDto.getEmail())
                .name(userDto.getName())
                .build();
    }

    public static User toUser(UserCreateDto userDto) {
        return User.builder()
                .email(userDto.getEmail())
                .name(userDto.getName())
                .build();
    }

    public static User updateIfDifferent(final User user, final UserDto userWithChanges) {
        return User.builder()
                .id(user.getId())
                .email(getChanged(user.getEmail(), userWithChanges.getEmail()))
                .name(getChanged(user.getName(), userWithChanges.getName()))
                .build();
    }

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