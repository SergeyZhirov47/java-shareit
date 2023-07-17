package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.exception.EmailAlreadyUsedException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto getById(long id) {
        return UserMapper.toUserDto(getUserById(id));
    }

    @Override
    public List<UserDto> getAll() {
        final List<User> users = userRepository.findAll();
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Long create(UserCreateDto userDto) {
        // Проверка, что еще нет пользователя с таким email.
        // checkExistsUserEmail(userDto.getEmail());

        User user = UserMapper.toUser(userDto);

        try {
            user = userRepository.save(user);
        }
        catch (DataIntegrityViolationException exp) {
            throw new EmailAlreadyUsedException(String.format("Пользователь с email = %s уже существует!", user.getEmail()));
        }

        return user.getId();
    }

    @Override
    public UserDto createAndGet(UserCreateDto userDto) {
        final Long id = create(userDto);
        return getById(id);
    }

    @Override
    public UserDto update(long id, UserDto userDto) {
        // Получение и проверка, что пользователь есть.
        final User user = getUserById(id);

        // Формируем пользователя с измененными полями.
        final User changedUser = UserMapper.updateIfDifferent(user, userDto);

        User updatedUser;
        if (user.equals(changedUser)) {
            updatedUser = user;
        } else {
            // Если все-таки различия есть, то проверяем почту.
            final String oldEmail = user.getEmail();
            final String newEmail = changedUser.getEmail();

            if (!oldEmail.equals(newEmail) && checkNotBlankEmail(newEmail)) {
                checkExistsUserEmail(newEmail);
            }

            updatedUser = userRepository.save(changedUser);
        }

        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void delete(long id) {
        // Потом наверное будут нужны доп. проверки.
        // Нельзя удалять пользователя, у которого есть вещи. Ну вещи есть, но они не используются.
        userRepository.deleteById(id);
    }

    private boolean checkNotBlankEmail(final String email) {
        return nonNull(email) && !email.isBlank();
    }

    private void checkExistsUserEmail(final String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(String.format("Пользователь с email = %s уже существует!", email));
        }
    }

    private User getUserById(long id) {
        final Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(id);
        }

        return userOpt.get();
    }
}
