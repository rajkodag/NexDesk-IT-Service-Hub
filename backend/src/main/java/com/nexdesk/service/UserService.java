package com.nexdesk.service;

import com.nexdesk.dto.UserResponse;
import com.nexdesk.entity.User;
import com.nexdesk.exception.ResourceNotFoundException;
import com.nexdesk.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    public UserResponse getUserById(Long id) {
        return toResponse(getUserEntity(id));
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUserRole(Long id, String role) {
        User user = getUserEntity(id);
        user.setRole(role.toUpperCase());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    public UserResponse toResponse(User u) {
        return new UserResponse(
                u.getUserId(),
                u.getFullName(),
                u.getEmail(),
                u.getRole(),
                u.getCreatedAt() != null ? u.getCreatedAt().toString() : null
        );
    }
}
