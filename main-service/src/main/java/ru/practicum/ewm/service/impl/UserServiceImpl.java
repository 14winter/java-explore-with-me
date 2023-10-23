package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.exception.UserAlreadyExistException;
import ru.practicum.ewm.exception.UserNotFoundException;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.UserService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(NewUserRequest newUserRequest) {
        isExistByName(newUserRequest.getName());
        User createdUser = userRepository.save(UserMapper.toUser(newUserRequest));
        log.info("Добавлен пользователь с id{}", createdUser.getId());
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<UserDto> getUsers(List<Long> ids, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<User> users;
        if (ids != null) {
            users = userRepository.findByIdIn(ids, pageRequest);
        } else {
            users = userRepository.findAll(pageRequest).stream().collect(Collectors.toList());
        }
        log.info("Получено пользователей: {}", users.size());
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
        log.info("Пользователь с id{} удален", id);
    }

    private void isExistByName(String name) {
        if (userRepository.existsByName(name)) {
            throw new UserAlreadyExistException("Пользователь с именем " + name + " уже существует.");
        }
    }
}
