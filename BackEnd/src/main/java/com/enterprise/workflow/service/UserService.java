package com.enterprise.workflow.service;

import com.enterprise.workflow.entity.User;
import com.enterprise.workflow.enums.Role;
import com.enterprise.workflow.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── Create new user ──────────────────────────────────────────
    public User createUser(String name, String email,
                           String password, Role role) {

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered: " + email);
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        return userRepository.save(user);
    }

    // ─── Get all users ────────────────────────────────────────────
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ─── Get user by ID ───────────────────────────────────────────
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }
    public List<User> getEmployees() {
        return userRepository.findByRole(Role.EMPLOYEE);
    }

    public long countEmployees() {
        return userRepository.countByRole(Role.EMPLOYEE);
    }

    // ─── Get user by email ────────────────────────────────────────
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "No user found with email: " + email));
    }
}