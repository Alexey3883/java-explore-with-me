package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto addUserAdmin(NewUserRequest newUserRequest) {
        if (newUserRequest == null) {
            throw new java.lang.IllegalArgumentException("Данные пользователя не могут быть null");
        }

        if (newUserRequest.getEmail() == null) {
            throw new java.lang.IllegalArgumentException("Адрес электронной почты не может быть null");
        }

        if (newUserRequest.getEmail().trim().isEmpty()) {
            throw new java.lang.IllegalArgumentException("Адрес электронной почты не может быть пустмы или состоять только из пробелов");
        }

        if (newUserRequest.getName() == null) {
            throw new java.lang.IllegalArgumentException("Имя не может быть null");
        }

        if (newUserRequest.getName().trim().isEmpty()) {
            throw new java.lang.IllegalArgumentException("Имя не может быть пустым или состоять только из пробелов");
        }

        if (newUserRequest.getName().length() < 2 || newUserRequest.getName().length() > 250) {
            throw new java.lang.IllegalArgumentException("Имя должно содержать от 2 до 250 символов");
        }

        if (newUserRequest.getEmail().length() < 6 || newUserRequest.getEmail().length() > 254) {
            throw new java.lang.IllegalArgumentException("Адрес электронной почты должен содержать от 6 до 254 символов");
        }

        int atIndex = newUserRequest.getEmail().indexOf('@');
        if (atIndex == -1 || atIndex == 0 || atIndex == newUserRequest.getEmail().length() - 1) {
            throw new java.lang.IllegalArgumentException("Электронное письмо должно содержать ровно один символ @, не в начале и не в конце");
        }

        String localPart = newUserRequest.getEmail().substring(0, atIndex);
        String domainPart = newUserRequest.getEmail().substring(atIndex + 1);

        if (localPart.contains("..") || domainPart.contains("..")) {
            throw new java.lang.IllegalArgumentException("Адрес электронной почты не может содержать точки");
        }

        if (localPart.startsWith(".") || localPart.endsWith(".") || domainPart.startsWith(".") || domainPart.endsWith(".")) {
            throw new java.lang.IllegalArgumentException("Адрес электронной почты не может начинаться или заканчиваться точкой в локальной или доменной части");
        }

        if (localPart.length() > 64) {
            throw new java.lang.IllegalArgumentException("Локальная часть адреса электронной почты не должн  превышать 64 символа");
        }

        if (domainPart.length() > 253) {
            throw new java.lang.IllegalArgumentException("Часть домена электронной почты должна содержать не более 253 символов");
        }

        if (!domainPart.matches("^[a-zA-Z0-9.-]+$")) {
            throw new java.lang.IllegalArgumentException("Домен электронной почты содержит недопустимые символы");
        }

        String[] domainParts = domainPart.split("\\.");
        for (String part : domainParts) {
            if (part.startsWith("-") || part.endsWith("-")) {
                throw new java.lang.IllegalArgumentException("Доменные имена не могут начинаться или заканчиваться дефисами");
            }
            if (part.length() > 63) {
                throw new java.lang.IllegalArgumentException("Длина каждого доменного имени не должна превышать 63 символа");
            }
        }

        log.info("Добавление нового пользователя: {}", newUserRequest);

        if (isUserExistByEmail(newUserRequest.getEmail())) {
            log.info("Нарушение целостности данных");
            throw new ru.practicum.exception.IllegalArgumentException(
                    "Пользователь с email: " + newUserRequest.getEmail() + " уже зарегистрирован"
            );
        }

        log.info("Пользователь зарегистрирован");
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(newUserRequest)));
    }

    @Override
    public List<UserDto> getUsersByIdsAdmin(List<Long> ids, Pageable pageable) {
        log.info("Получение списка пользователей по id {}", ids);
        if (ids == null || ids.isEmpty()) {
            return userRepository.findAll(pageable).stream()
                    .map(userMapper::toUserDto)
                    .collect(Collectors.toList());
        }
        return userRepository.findByIdIn(ids).stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUserAdmin(Long userId) {

        log.info("Удаление пользователя с id: {}", userId);

        if (userRepository.existsById(userId)) {
            log.info("Пользователь удален");
            userRepository.deleteById(userId);
        } else {
            log.info("Пользователь не найден или недоступен");
            throw new NotFoundException("Пользователь с id: " + userId + " не найден");
        }

    }

    private boolean isUserExistByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}