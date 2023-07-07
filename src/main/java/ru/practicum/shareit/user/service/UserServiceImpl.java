package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.NotFoundException;
import ru.practicum.shareit.user.exception.EmailAlreadyUsedException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
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
        final List<User> users = userRepository.getAll();
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Long create(UserCreateDto userDto) {
        // Проверка, что еще нет пользователя с таким email.
        checkExistsUserEmail(userDto.getEmail());

        final User user = UserMapper.toUser(userDto);
        return userRepository.create(user);
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

            updatedUser = userRepository.update(changedUser);
        }

        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void delete(long id) {
        // ToDo
        // что будет со всеми вещами пользователя?
        // Запретить удалять, если остались вещи в пользовании?
        userRepository.delete(id);
    }

    private boolean checkNotBlankEmail(final String email) {
        return nonNull(email) && !email.isBlank();
    }

    private void checkExistsUserEmail(final String email) {
        if (userRepository.containsByEmail(email)) {
            throw new EmailAlreadyUsedException(String.format("Пользователь с email = %s уже существует!", email));
        }
    }

    private User getUserById(Long id) {
        final Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            throwNotFoundExceptionForUser(id);
        }

        return userOpt.get();
    }

    // ToDo
    // Вынести в отдельное исключение?
    private void throwNotFoundExceptionForUser(Long id) {
        throw new NotFoundException(String.format("Пользователь с id = %s не найден", id));
    }
}
