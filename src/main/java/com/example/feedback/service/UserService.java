package com.example.feedback.service;

import com.example.feedback.dto.RegisterRequest;
import com.example.feedback.entity.ActivityType;
import com.example.feedback.entity.Role;
import com.example.feedback.entity.User;
import com.example.feedback.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserActivityService userActivityService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserActivityService userActivityService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userActivityService = userActivityService;
    }

    public User saveUser(User user) {
        if (user.getId() == null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            Optional<User> existing = userRepository.findById(user.getId());
            if (existing.isPresent() && !user.getPassword().equals(existing.get().getPassword()) && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else if (existing.isPresent()) {
                user.setPassword(existing.get().getPassword());
            }
        }
        User saved = userRepository.save(user);
        userActivityService.logActivity(saved, ActivityType.REGISTRATION, "New user created/updated: " + saved.getEmail());
        return saved;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void toggleUserStatus(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            if ("Active".equalsIgnoreCase(user.getStatus())) {
                user.setStatus("Inactive");
            } else {
                user.setStatus("Active");
            }
            userRepository.save(user);
        });
    }
    
    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getUsersByRoleCount(Role role) {
        return userRepository.countByRole(role);
    }
    
    public int getActiveUsersCount() {
        return userRepository.countByStatus("Active");
    }

    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllUsers();
        }
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword.trim(), keyword.trim());
    }

    /**
     * Registers a new CLIENT user from the registration form.
     * @throws IllegalArgumentException if email is already taken or passwords don't match
     */
    public User registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CLIENT);
        user.setStatus("Active");
        User saved = userRepository.save(user);
        userActivityService.logActivity(saved, ActivityType.REGISTRATION, "Faculty account registered");
        return saved;
    }
}
