package com.example.novawear.service;

import com.example.novawear.dto.UserCreateRequest;
import com.example.novawear.dto.UserResponse;
import com.example.novawear.dto.UserUpdateRequest;
import com.example.novawear.entity.User;
import com.example.novawear.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User u = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return UserResponse.from(u);
    }

    @Transactional
    public UserResponse create(UserCreateRequest req) {
        if (userRepository.existsByUsername(req.getUsername().trim())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(req.getEmail().trim())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        User.Role role = parseRole(req.getRole());
        User u = User.builder()
                .username(req.getUsername().trim())
                .email(req.getEmail().trim())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .active(true)
                .build();
        u = userRepository.save(u);
        return UserResponse.from(u);
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest req) {
        User u = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        String username = req.getUsername() != null ? req.getUsername().trim() : "";
        String email = req.getEmail() != null ? req.getEmail().trim() : "";
        if (username.isEmpty()) throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        if (email.isEmpty()) throw new IllegalArgumentException("Email không được để trống");
        if (!u.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (!u.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        u.setUsername(username);
        u.setEmail(email);
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            if (req.getPassword().length() < 6) {
                throw new IllegalArgumentException("Mật khẩu tối thiểu 6 ký tự");
            }
            u.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getRole() != null) {
            u.setRole(parseRole(req.getRole()));
        }
        if (req.getActive() != null) {
            u.setActive(req.getActive());
        }
        u = userRepository.save(u);
        return UserResponse.from(u);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserResponse updateActive(Long id, boolean active) {
        User u = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        u.setActive(active);
        u = userRepository.save(u);
        return UserResponse.from(u);
    }

    private static User.Role parseRole(String role) {
        if (role == null || role.isBlank()) return User.Role.USER;
        try {
            return User.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return User.Role.USER;
        }
    }
}
