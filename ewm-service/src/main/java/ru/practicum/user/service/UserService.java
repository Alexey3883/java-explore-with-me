package ru.practicum.user.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto addUserAdmin(NewUserRequest newUserRequest);

    List<UserDto> getUsersByIdsAdmin(List<Long> ids, Pageable pageable);

    void deleteUserAdmin(Long id);

}