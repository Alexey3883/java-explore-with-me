package ru.practicum.user.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

@RequiredArgsConstructor
@Component
public class UserMapper {

    public User toUser(NewUserRequest newUserRequest) {
        if (newUserRequest == null) {
            return null;
        }

        return User.builder()
                .email(newUserRequest.getEmail())
                .name(newUserRequest.getName())
                .build();
    }

    public UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public NewUserRequest toNewUserRequest(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        return NewUserRequest.builder()
                .email(userDto.getEmail())
                .name(userDto.getName())
                .build();
    }

    public UserShortDto toUserShortDto(User user) {
        if (user == null) {
            return null;
        }

        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public User toUserShort(UserShortDto userShortDto) {
        if (userShortDto == null) {
            return null;
        }

        return User.builder()
                .id(userShortDto.getId())
                .name(userShortDto.getName())
                .build();
    }

}