package ru.practicum.shareit.user.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.common.IdGenerator;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final HashMap<Long, User> users;
    private final IdGenerator idGenerator = new IdGenerator(0L);

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> getAll() {
        return users.values().stream().collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Long create(User user) {
        final Long newId = idGenerator.getNext();
        user.setId(newId);
        users.put(newId, user);

        return newId;
    }

    @Override
    public User update(User user) {
        final Long userId = user.getId();
        users.put(userId, user);
        return users.get(userId);
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }

    @Override
    public boolean containsById(Long id) {
        return users.containsKey(id);
    }

    @Override
    public boolean containsByEmail(String email) {
        return users.values().stream().anyMatch(u -> u.getEmail().equals(email));
    }
}
