package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);

    List<User> getAll();

    Long create(User user);

    User update(User user);

    void delete(Long id);

    boolean containsById(Long id);

    boolean containsByEmail(String email);
}
