package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

public interface UserCustomRepository {
    User getUserById(long id);

    void checkUserExists(long id);
}