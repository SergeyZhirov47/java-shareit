package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.model.User;

public interface JPAUserRepository extends JpaRepository<User, Long>  {
    boolean existsByEmail(String email);
}
