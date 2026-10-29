package com.enterprise.workflow.controller;

import com.enterprise.workflow.entity.User;
import com.enterprise.workflow.enums.Role;
import com.enterprise.workflow.security.JwtUtil;
import com.enterprise.workflow.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;                    // ← add this field
    private final PasswordEncoder passwordEncoder;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // find user by email
            User user = userService.getUserByEmail(request.getEmail());

            // check password matches
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid password");
            }

            // generate JWT token
            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getRole().name()
            );

            // return token + user info
            return ResponseEntity.ok(new LoginResponse(
                    token,
                    user.getName(),
                    user.getRole().name(),
                    user.getId()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }

    // ─── inner classes ────────────────────────────────────────────────
    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private String name;
        private String role;
        private Long userId;

        public LoginResponse(String token, String name,
                             String role, Long userId) {
            this.token = token;
            this.name = name;
            this.role = role;
            this.userId = userId;
        }
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        try {
            User user = userService.createUser(
                    request.getName(),
                    request.getEmail(),
                    request.getPassword(),
                    Role.valueOf(request.getRole())
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Data
    public static class RegisterRequest{
        private String name;
        private String email;
        private String password;
        private String role; // "ADMIN", "MANAGER", "EMPLOYEE"

        // getters and setters

    }
}
