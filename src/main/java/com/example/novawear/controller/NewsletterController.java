package com.example.novawear.controller;

import com.example.novawear.dto.SubscriberDto;
import com.example.novawear.service.SubscriberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final SubscriberService subscriberService;

    @PostMapping("/subscribe")
    public ResponseEntity<SubscriberDto> subscribe(@Valid @RequestBody SubscriberDto dto) {
        return ResponseEntity.ok(subscriberService.subscribe(dto.getEmail()));
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Map<String, String>> unsubscribe(@RequestBody Map<String, String> body) {
        subscriberService.unsubscribe(body.get("email"));
        return ResponseEntity.ok(Map.of("message", "Đã hủy đăng ký thành công"));
    }
}
