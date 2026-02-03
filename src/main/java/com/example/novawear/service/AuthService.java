package com.example.novawear.service;

import com.example.novawear.dto.LoginRequest;
import com.example.novawear.dto.LoginResponse;
import com.example.novawear.dto.RegisterRequest;
import com.example.novawear.dto.UserResponse;
import com.example.novawear.entity.User;
import com.example.novawear.repository.UserRepository;
import com.example.novawear.security.JwtUtil;
import lombok.RequiredArgsConstructor;
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
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
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
}
