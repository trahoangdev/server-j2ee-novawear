package com.example.novawear.service;

import com.example.novawear.dto.UserResponse;
import com.example.novawear.entity.User;
import com.example.novawear.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
    public UserResponse updateActive(Long id, boolean active) {
        User u = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        u.setActive(active);
        u = userRepository.save(u);
        return UserResponse.from(u);
    }
}
