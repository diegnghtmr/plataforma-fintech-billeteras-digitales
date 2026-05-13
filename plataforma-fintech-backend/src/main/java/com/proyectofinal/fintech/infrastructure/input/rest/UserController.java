package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.usecase.CreateUserUseCase;
import com.proyectofinal.fintech.application.usecase.DeleteUserUseCase;
import com.proyectofinal.fintech.application.usecase.GetUserUseCase;
import com.proyectofinal.fintech.application.usecase.ListUsersUseCase;
import com.proyectofinal.fintech.application.usecase.UpdateUserUseCase;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.CreateUserRequestDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.UpdateUserRequestDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.UserResponseDto;
import com.proyectofinal.fintech.infrastructure.mapper.UserMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST adapter for user operations.
 * Context-path /api/v1 set in application.properties (SDD 1).
 * Paths here are bare: /users, /users/{userId}.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UserMapper userMapper;

    public UserController(CreateUserUseCase createUserUseCase,
                          GetUserUseCase getUserUseCase,
                          ListUsersUseCase listUsersUseCase,
                          UpdateUserUseCase updateUserUseCase,
                          DeleteUserUseCase deleteUserUseCase,
                          UserMapper userMapper) {
        this.createUserUseCase = createUserUseCase;
        this.getUserUseCase = getUserUseCase;
        this.listUsersUseCase = listUsersUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> listUsers() {
        List<UserResponseDto> dtos = listUsersUseCase.execute().stream()
                .map(userMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody CreateUserRequestDto request) {
        createUserUseCase.execute(request.id(), request.name(), request.email());
        UserResponseDto dto = userMapper.toDto(getUserUseCase.execute(request.id()));
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable String userId) {
        UserResponseDto dto = userMapper.toDto(getUserUseCase.execute(userId));
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable String userId,
                                                       @Valid @RequestBody UpdateUserRequestDto request) {
        var view = updateUserUseCase.execute(userId,
                Optional.ofNullable(request.name()),
                Optional.ofNullable(request.email()));
        return ResponseEntity.ok(userMapper.toDto(view));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        deleteUserUseCase.execute(userId);
        return ResponseEntity.noContent().build();
    }
}
