package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Repository
public interface DaoUser {
    User getUserById(long id);

    void checkUserExists(long id);

    List<User> findAll();

    User save(User entity);

    boolean existsByEmail(String email);

    boolean existsById(long id);

    void deleteById(long id);

    void deleteAll();
}
