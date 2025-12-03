package ru.practicum.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @ResponseBody
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUserAdmin(@Valid @RequestBody NewUserRequest newUserRequest) {
        log.info("Получен запрос на добавление нового пользователя");

        return userService.addUserAdmin(newUserRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsersByIdsAdmin(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "10") int size) {
        log.info("Получен запрос на получение информации о пользователях");
        return userService.getUsersByIdsAdmin(ids, PageRequest.of(from, size));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserAdmin(@PathVariable Long userId) {
        userService.deleteUserAdmin(userId);
    }
}