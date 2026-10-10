package com.nexdesk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexdesk.dto.LoginRequest;
import com.nexdesk.dto.LoginResponse;
import com.nexdesk.dto.RegisterRequest;
import com.nexdesk.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void testRegisterEndpoint_Success() throws Exception {
        RegisterRequest req = new RegisterRequest("Test User", "test@nexdesk.com", "Password@123!", "USER");
        LoginResponse resp = new LoginResponse("mock.jwt.token", 1L, "Test User", "test@nexdesk.com", "USER");

        when(authService.register(any(RegisterRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@nexdesk.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testLoginEndpoint_Success() throws Exception {
        LoginRequest req = new LoginRequest("test@nexdesk.com", "password123");
        LoginResponse resp = new LoginResponse("valid.jwt.token", 1L, "Test User", "test@nexdesk.com", "USER");

        when(authService.login(any(LoginRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("valid.jwt.token"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void testRegisterEndpoint_ValidationFailure() throws Exception {
        // Missing required fields (empty email and short password)
        RegisterRequest req = new RegisterRequest("", "invalid-email", "123", "USER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    void testResetPasswordEndpoint_Success() throws Exception {
        com.nexdesk.dto.ResetPasswordRequest req = new com.nexdesk.dto.ResetPasswordRequest("test@nexdesk.com", "BrandNew@2026!");

        doNothing().when(authService).resetPassword(any(com.nexdesk.dto.ResetPasswordRequest.class));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());
    }
}
