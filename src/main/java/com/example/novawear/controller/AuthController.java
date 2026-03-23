package com.example.novawear.controller;

import com.example.novawear.dto.ChangePasswordRequest;
import com.example.novawear.dto.LoginRequest;
import com.example.novawear.dto.LoginResponse;
import com.example.novawear.dto.GoogleLoginRequest;
import com.example.novawear.dto.ProfileUpdateRequest;
import com.example.novawear.dto.RegisterRequest;
import com.example.novawear.dto.UserResponse;
import com.example.novawear.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        LoginResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        UserResponse response = authService.me(user.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody ProfileUpdateRequest request) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        UserResponse response = authService.updateProfile(user.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody ChangePasswordRequest request) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        authService.changePassword(user.getUsername(), request);
        return ResponseEntity.ok().body(java.util.Map.of("message", "Đổi mật khẩu thành công"));
    }
}
