package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto getById(long id);

    List<UserDto> getAll();

    Long create(UserCreateDto userDto);

    UserDto createAndGet(UserCreateDto userDto);

    UserDto update(long id, UserDto userDto);

    void delete(long id);
}
