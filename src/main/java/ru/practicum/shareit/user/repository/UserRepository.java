package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @Query("SELECT case when count(u)> 0 then true else false end FROM User as u WHERE u.email = :email AND u.id != :userId")
    boolean isOtherUserHasSameEmail(@Param("email") String email, @Param("userId") long userId);
}
