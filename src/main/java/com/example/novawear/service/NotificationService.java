package com.example.novawear.service;

import com.example.novawear.dto.NotificationDto;
import com.example.novawear.entity.Notification;
import com.example.novawear.entity.User;
import com.example.novawear.repository.NotificationRepository;
import com.example.novawear.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public NotificationDto create(Long userId, String type, String title, String message, String linkTo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Notification n = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .linkTo(linkTo)
                .build();
        n = notificationRepository.save(n);
        return NotificationDto.from(n);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> findByUser(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationDto::from);
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationDto markAsRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + id));
        n.setIsRead(true);
        n = notificationRepository.save(n);
        return NotificationDto.from(n);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        var page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId,
                org.springframework.data.domain.Pageable.unpaged());
        page.getContent().forEach(n -> {
            if (!n.getIsRead()) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
        });
    }

    @Transactional
    public void deleteRead(Long userId) {
        var page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId,
                org.springframework.data.domain.Pageable.unpaged());
        page.getContent().forEach(n -> {
            if (n.getIsRead()) {
                notificationRepository.delete(n);
            }
        });
    }
}
