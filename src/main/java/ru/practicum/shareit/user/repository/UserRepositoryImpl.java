package ru.practicum.shareit.user.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.common.IdGenerator;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Component
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> userEmails = new HashSet<>();
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
        userEmails.add(user.getEmail());

        return newId;
    }

    @Override
    public User update(User user) {
        final Long userId = user.getId();

        final User oldUser = users.get(userId);
        userEmails.remove(oldUser.getEmail());

        users.put(userId, user);
        userEmails.add(user.getEmail());

        return user;
    }

    @Override
    public void delete(Long id) {
        final User user = users.get(id);
        if (isNull(user)) {
            return;
        }

        userEmails.remove(user.getEmail());
        users.remove(id);
    }

    @Override
    public boolean containsById(Long id) {
        return users.containsKey(id);
    }

    @Override
    public boolean containsByEmail(String email) {
        return userEmails.contains(email);
    }
}
