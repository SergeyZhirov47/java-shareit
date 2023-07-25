package ru.practicum.shareit.user.repository;

import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {
    private final JPAUserRepository jpaUserRepository;

    @Override
    public User getUserById(long id) {
        final Optional<User> userOpt = jpaUserRepository.findById(id);
        return userOpt.orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public void checkUserExists(long id) {
        if (!jpaUserRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
    }
}