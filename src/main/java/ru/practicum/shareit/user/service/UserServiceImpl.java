package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.exception.EmailAlreadyUsedException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.DaoUser;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final DaoUser daoUser;

    @Transactional(readOnly = true)
    @Override
    public UserDto getById(long id) {
        return UserMapper.toUserDto(daoUser.getUserById(id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAll() {
        final List<User> users = daoUser.findAll();
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toUnmodifiableList());
    }

    @Transactional
    @Override
    public Long create(UserCreateDto userDto) {
        User user = UserMapper.toUser(userDto);

        // По тестам postman получается, что теперь БД будет "проверять" уникальность email.
        try {
            user = daoUser.save(user);
        } catch (DataIntegrityViolationException exp) {
            throw new EmailAlreadyUsedException(String.format("Пользователь с email = %s уже существует!", user.getEmail()));
        }

        return user.getId();
    }

    @Transactional
    @Override
    public UserDto createAndGet(UserCreateDto userDto) {
        final Long id = create(userDto);
        return getById(id);
    }

    @Transactional
    @Override
    public UserDto update(long id, UserDto userDto) {
        // Получение и проверка, что пользователь есть.
        final User user = daoUser.getUserById(id);
        // Формируем пользователя с измененными полями.
        final User changedUser = UserMapper.updateIfDifferent(user, userDto);
        // Проверяем, что еще нет пользователя с такой же почтой (и это не наш текущий пользователь)
        checkExistsUserEmail(changedUser.getEmail(), id);
        final User updatedUser = daoUser.save(changedUser);

        return UserMapper.toUserDto(updatedUser);
    }

    @Transactional
    @Override
    public void delete(long id) {
        // Потом наверное будут нужны доп. проверки.
        // Нельзя удалять пользователя, у которого есть вещи. Ну вещи есть, но они не используются.
        daoUser.deleteById(id);
    }

    private boolean checkNotBlankEmail(final String email) {
        return nonNull(email) && !email.isBlank();
    }

    private void checkExistsUserEmail(final String email, long userId) {
        if (daoUser.isOtherUserHasSameEmail(email, userId)) {
            throw new EmailAlreadyUsedException(String.format("Пользователь с email = %s уже существует!", email));
        }
    }
}
