package com.example.novawear.controller.admin;

import com.example.novawear.dto.SubscriberDto;
import com.example.novawear.service.SubscriberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/subscribers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubscriberController {

    private final SubscriberService subscriberService;

    @GetMapping
    public ResponseEntity<Page<SubscriberDto>> list(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(subscriberService.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count() {
        return ResponseEntity.ok(Map.of("active", subscriberService.countActive()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subscriberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
