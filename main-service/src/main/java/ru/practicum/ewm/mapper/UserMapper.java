package ru.practicum.ewm.mapper;

import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.dto.UserShortDto;
import ru.practicum.ewm.model.User;

public class UserMapper {
    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toUser(NewUserRequest newUserRequest) {
        return User.builder()
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build();
    }

    public static UserShortDto toUserShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
