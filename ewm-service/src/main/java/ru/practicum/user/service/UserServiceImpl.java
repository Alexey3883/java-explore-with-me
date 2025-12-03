package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.IllegalArgumentException;
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

        // Проверка валидации
        if (newUserRequest == null) {
            throw new java.lang.IllegalArgumentException("User data cannot be null");
        }
        
        if (newUserRequest.getEmail() == null) {
            throw new java.lang.IllegalArgumentException("Email cannot be null");
        }
        
        if (newUserRequest.getEmail().trim().isEmpty()) {
            throw new java.lang.IllegalArgumentException("Email cannot be empty or consist of spaces only");
        }
        
        if (newUserRequest.getName() == null) {
            throw new java.lang.IllegalArgumentException("Name cannot be null");
        }
        
        if (newUserRequest.getName().trim().isEmpty()) {
            throw new java.lang.IllegalArgumentException("Name cannot be empty or consist of spaces only");
        }
        
        // Проверка длины строк
        if (newUserRequest.getName().length() < 2 || newUserRequest.getName().length() > 250) {
            throw new java.lang.IllegalArgumentException("Name must be between 2 and 250 characters");
        }
        
        if (newUserRequest.getEmail().length() < 6 || newUserRequest.getEmail().length() > 254) {
            throw new java.lang.IllegalArgumentException("Email must be between 6 and 254 characters");
        }

        log.info("Добавление нового пользователя: {}", newUserRequest);

        if (isUserExistByEmail(newUserRequest.getEmail())) {
            log.info("Нарушение целостности данных");
            throw new IllegalArgumentException(
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