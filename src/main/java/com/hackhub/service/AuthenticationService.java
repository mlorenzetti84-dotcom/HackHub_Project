package com.hackhub.service;

import com.hackhub.domain.actor.User;
import com.hackhub.domain.exception.ValidationException;
import com.hackhub.domain.exception.AuthenticationException;
import com.hackhub.dto.UserDto;
import com.hackhub.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto register(String username, String email, String password) {
        validatePassword(password);
        if (email == null || userRepository.existsByEmailIgnoreCase(email.trim())) {
            throw new ValidationException("Email is already registered");
        }
        User user = new User(username, email);
        user.setPasswordHash(passwordEncoder.encode(password));
        return UserDto.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthenticatedUser login(String email, String password) {
        User user = userRepository.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }
        String token = UUID.randomUUID().toString();
        sessions.put(token, user.getId());
        return new AuthenticatedUser(token, UserDto.from(user));
    }

    @Transactional(readOnly = true)
    public UserDto currentUser(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthenticationException("Access token is required");
        }
        Long userId = sessions.get(token);
        if (userId == null) {
            throw new AuthenticationException("Invalid or expired access token");
        }
        return UserDto.from(userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId)));
    }

    public void logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new ValidationException("Password must contain at least 8 characters");
        }
    }

    public record AuthenticatedUser(String accessToken, UserDto user) {
    }
}
