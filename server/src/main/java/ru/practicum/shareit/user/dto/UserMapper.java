package ru.practicum.shareit.user.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.common.AbstractMapper;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class UserMapper extends AbstractMapper {
    public UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public User toUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .email(userDto.getEmail())
                .name(userDto.getName())
                .build();
    }

    public User toUser(UserCreateDto userDto) {
        return User.builder()
                .email(userDto.getEmail())
                .name(userDto.getName())
                .build();
    }

    public User updateIfDifferent(final User user, final UserDto userWithChanges) {
        return User.builder()
                .id(user.getId())
                .email(getChanged(user.getEmail(), userWithChanges.getEmail()))
                .name(getChanged(user.getName(), userWithChanges.getName()))
                .build();
    }
}