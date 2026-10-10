package com.nexdesk;

import com.nexdesk.dto.LoginRequest;
import com.nexdesk.dto.LoginResponse;
import com.nexdesk.dto.RegisterRequest;
import com.nexdesk.entity.User;
import com.nexdesk.exception.BadRequestException;
import com.nexdesk.exception.UnauthorizedException;
import com.nexdesk.repository.UserRepository;
import com.nexdesk.security.JwtService;
import com.nexdesk.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("John Doe", "john@example.com", "encodedPass", "USER");
        sampleUser.setUserId(1L);
    }

    @Test
    void testRegister_Success() {
        RegisterRequest req = new RegisterRequest("John Doe", "john@example.com", "Secret@123!", "USER");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123!")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtService.generateToken(anyString(), anyLong(), anyString(), anyString())).thenReturn("mock.jwt.token");

        LoginResponse resp = authService.register(req);

        assertNotNull(resp);
        assertEquals("mock.jwt.token", resp.getToken());
        assertEquals("john@example.com", resp.getEmail());
        assertEquals(1L, resp.getUserId());
        assertEquals("USER", resp.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        RegisterRequest req = new RegisterRequest("Duplicate", "john@example.com", "pass", "USER");
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        LoginRequest req = new LoginRequest("john@example.com", "secret123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("secret123", "encodedPass")).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyLong(), anyString(), anyString())).thenReturn("valid.jwt.token");

        LoginResponse resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("valid.jwt.token", resp.getToken());
        assertEquals(1L, resp.getUserId());
    }

    @Test
    void testLogin_InvalidPassword() {
        LoginRequest req = new LoginRequest("john@example.com", "wrongpass");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongpass", "encodedPass")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }

    @Test
    void testResetPassword_Success() {
        com.nexdesk.dto.ResetPasswordRequest req = new com.nexdesk.dto.ResetPasswordRequest("john@example.com", "NewSecret@2026!");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.encode("NewSecret@2026!")).thenReturn("newEncodedPass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        assertDoesNotThrow(() -> authService.resetPassword(req));
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void testResetPassword_UserNotFound() {
        com.nexdesk.dto.ResetPasswordRequest req = new com.nexdesk.dto.ResetPasswordRequest("unknown@example.com", "NewSecret@2026!");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(com.nexdesk.exception.ResourceNotFoundException.class, () -> authService.resetPassword(req));
        verify(userRepository, never()).save(any(User.class));
    }
}
