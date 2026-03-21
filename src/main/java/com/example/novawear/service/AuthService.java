package com.example.novawear.service;

import com.example.novawear.dto.ChangePasswordRequest;
import com.example.novawear.dto.LoginRequest;
import com.example.novawear.dto.LoginResponse;
import com.example.novawear.dto.ProfileUpdateRequest;
import com.example.novawear.dto.RegisterRequest;
import com.example.novawear.dto.UserResponse;
import com.example.novawear.entity.User;
import com.example.novawear.repository.UserRepository;
import com.example.novawear.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String input = request.getUsername() != null ? request.getUsername().trim() : "";
        if (input.isEmpty()) {
            throw new BadCredentialsException("Username or email required");
        }
        // Allow login with username or email: resolve to username
        String username;
        if (input.contains("@")) {
            username = userRepository.findByEmail(input)
                    .map(User::getUsername)
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        } else {
            username = input;
        }
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole().name());
        return LoginResponse.from(user, token);
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(User.Role.USER)
                .active(true)
                .build();
        user = userRepository.save(user);
        return UserResponse.from(user);
    }

    public UserResponse me(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(String username, ProfileUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update email if changed
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác");
                }
                user.setEmail(newEmail);
            }
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName().trim().isEmpty() ? null : request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim().isEmpty() ? null : request.getPhone().trim());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress().trim().isEmpty() ? null : request.getAddress().trim());
        }

        user = userRepository.save(user);
        return UserResponse.from(user);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
