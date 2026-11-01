
package com.enterprise.workflow.controller;

import com.enterprise.workflow.entity.User;
import com.enterprise.workflow.enums.Role;
import com.enterprise.workflow.repositories.UserRepository;
import com.enterprise.workflow.security.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String token(String email, String role) {
        return jwtUtil.generateToken(email, role);
    }

    private User createUser() {
        User user = new User();
        user.setName("Security Test User");
        user.setEmail("security-test@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.EMPLOYEE);
        return userRepository.save(user);
    }

    @Test
    void employeeShouldNotAccessUserEndpoints() throws Exception {
        String jwt = token("employee@example.com", "EMPLOYEE");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/users/1")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/users/summary")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerShouldAccessUserEndpoints() throws Exception {
        User user = createUser();
        String jwt = token("manager@example.com", "MANAGER");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/" + user.getId())
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/summary")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());
    }

    @Test
    void invalidJwtShouldBeRejected() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredJwtShouldBeRejected() throws Exception {
        Date issuedAt = new Date(System.currentTimeMillis() - 120000);
        Date expiration = new Date(System.currentTimeMillis() - 60000);

        String expiredToken = Jwts.builder()
                .setSubject("expired@example.com")
                .claim("role", "MANAGER")
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(
                        Keys.hmacShaKeyFor(
                                jwtSecret.getBytes(StandardCharsets.UTF_8)
                        ),
                        SignatureAlgorithm.HS256
                )
                .compact();

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationShouldIgnorePrivilegedRoleFromRequest()
            throws Exception {

        String requestBody = """
                {
                    "name": "New Employee",
                    "email": "new-employee@example.com",
                    "password": "Password123!",
                    "role": "ADMIN"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));

        User savedUser = userRepository
                .findByEmail("new-employee@example.com")
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                Role.EMPLOYEE, savedUser.getRole()
        );
    }
}
