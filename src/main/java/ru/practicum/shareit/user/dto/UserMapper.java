package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.common.AbstractMapper;
import ru.practicum.shareit.user.model.User;

public class UserMapper extends AbstractMapper {
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
}