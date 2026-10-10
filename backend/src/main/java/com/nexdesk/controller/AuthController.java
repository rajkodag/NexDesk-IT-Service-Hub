package com.nexdesk.controller;

import com.nexdesk.dto.LoginRequest;
import com.nexdesk.dto.LoginResponse;
import com.nexdesk.dto.RegisterRequest;
import com.nexdesk.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Authentication", description = "Endpoints for user registration and JWT login")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping({"/api/auth/register", "/api/signup"})
    @Operation(summary = "Register a new user account", description = "Creates a new user and returns a signed JWT token.")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping({"/api/auth/login", "/api/login"})
    @Operation(summary = "Authenticate user with email and password", description = "Validates credentials and returns JWT Bearer token.")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/api/auth/reset-password", "/api/reset-password"})
    @Operation(summary = "Reset forgotten password", description = "Validates identity and updates password with enterprise complexity standards.")
    public ResponseEntity<java.util.Map<String, Object>> resetPassword(@Valid @RequestBody com.nexdesk.dto.ResetPasswordRequest request) {
        authService.resetPassword(request);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("success", true);
        resp.put("message", "Password has been successfully reset. You may now sign in with your new password.");
        return ResponseEntity.ok(resp);
    }
}
