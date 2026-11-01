package com.enterprise.workflow.controller;




import com.enterprise.workflow.entity.User;
import com.enterprise.workflow.enums.Role;
import com.enterprise.workflow.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerShouldCreateEmployeeEvenIfAdminRoleIsRequested()
            throws Exception {

        String request = """
                {
                    "name": "Test User",
                    "email": "test@example.com",
                    "password": "Password123",
                    "role": "ADMIN"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));

        User savedUser = userRepository.findByEmail("test@example.com")
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                Role.EMPLOYEE, savedUser.getRole());

        org.junit.jupiter.api.Assertions.assertTrue(
                passwordEncoder.matches(
                        "Password123", savedUser.getPassword()));
    }

    @Test
    void registerShouldRejectDuplicateEmail() throws Exception {

        userRepository.save(createUser(
                "Existing User",
                "existing@example.com",
                "Password123",
                Role.EMPLOYEE
        ));

        String request = """
                {
                    "name": "Another User",
                    "email": "existing@example.com",
                    "password": "Password456"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginShouldReturnJwtForValidCredentials() throws Exception {

        userRepository.save(createUser(
                "Login User",
                "login@example.com",
                "Password123",
                Role.EMPLOYEE
        ));

        String request = """
                {
                    "email": "login@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Login User"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.userId").isNumber());
    }

    @Test
    void loginShouldRejectIncorrectPassword() throws Exception {

        userRepository.save(createUser(
                "Login User",
                "login@example.com",
                "Password123",
                Role.EMPLOYEE
        ));

        String request = """
                {
                    "email": "login@example.com",
                    "password": "WrongPassword"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginShouldRejectUnknownEmail() throws Exception {

        String request = """
                {
                    "email": "unknown@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());
    }

    private User createUser(
            String name,
            String email,
            String password,
            Role role) {

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        return user;
    }
}
