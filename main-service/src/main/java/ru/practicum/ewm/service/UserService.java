package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;

import java.util.Collection;
import java.util.List;

public interface UserService {
    UserDto create(NewUserRequest newUserRequest);

    Collection<UserDto> getUsers(List<Long> ids, int from, int size);

    void delete(Long id);
}
