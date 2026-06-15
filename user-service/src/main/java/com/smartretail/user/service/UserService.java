package com.smartretail.user.service;

import com.smartretail.user.dto.*;
import com.smartretail.user.entity.User;
import com.smartretail.user.entity.UserStatus;
import com.smartretail.user.exception.DuplicateResourceException;
import com.smartretail.user.exception.InvalidCredentialsException;
import com.smartretail.user.exception.ResourceNotFoundException;
import com.smartretail.user.messaging.EventPublisher;
import com.smartretail.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final EventPublisher eventPublisher;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email " + request.getEmail() + " already exists");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        User saved = userRepository.save(user);
        eventPublisher.publishUserRegistered(saved.getId(), saved.getName(), saved.getEmail());
        return mapToResponse(saved);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new InvalidCredentialsException("User account is inactive");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return LoginResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(String id) {
        return mapToResponse(findUserById(id));
    }

    public UserResponse updateUser(String id, UpdateUserRequest request) {
        User user = findUserById(id);
        if (request.getName() != null) user.setName(request.getName());
        if (request.getEmail() != null) {
            if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getRole() != null) user.setRole(request.getRole());
        return mapToResponse(userRepository.save(user));
    }

    public void deleteUser(String id) {
        findUserById(id);
        userRepository.deleteById(id);
    }

    public UserResponse activateUser(String id) {
        User user = findUserById(id);
        user.setStatus(UserStatus.ACTIVE);
        return mapToResponse(userRepository.save(user));
    }

    public UserResponse deactivateUser(String id) {
        User user = findUserById(id);
        user.setStatus(UserStatus.INACTIVE);
        return mapToResponse(userRepository.save(user));
    }

    private User findUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
