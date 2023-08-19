package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto create(@RequestBody UserCreateDto user) {
        log.info(String.format("POST /users, body = %s", user));
        final UserDto newUser = userService.createAndGet(user);
        log.info(String.format("Успешно создан пользователь с id = %s", newUser.getId()));

        return newUser;
    }

    @PatchMapping("/{id}")
    public UserDto update(@RequestBody UserDto user, @PathVariable(name = "id") long id) {
        log.info(String.format("PATCH /users/{id}, {id} = %s", id));
        final UserDto updatedUser = userService.update(id, user);
        log.info(String.format("Успешно обновлены данные пользователя с id = %s", updatedUser.getId()));

        return updatedUser;
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable(name = "id") long id) {
        log.info(String.format("GET /users/{id}, {id} = %s", id));
        final UserDto user = userService.getById(id);
        log.info(String.format("Успешно получены данные пользователя с id = %s", user.getId()));

        return user;
    }

    @GetMapping
    public List<UserDto> getAll() {
        log.info("GET /users/");
        final List<UserDto> users = userService.getAll();
        log.info("Успешно получены все пользователи");

        return users;
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable(name = "id") long id) {
        log.info(String.format("DELETE /users/{id}, {id} = %s", id));
        userService.delete(id);
        log.info(String.format("Успешно удален пользователь с id = %s", id));
    }
}
