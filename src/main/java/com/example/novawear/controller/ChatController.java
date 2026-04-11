package com.example.novawear.controller;

import com.example.novawear.dto.ChatRequest;
import com.example.novawear.service.RagChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    private final RagChatService ragChatService;

    public ChatController(RagChatService ragChatService) {
        this.ragChatService = ragChatService;
    }

    @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody ChatRequest request) {
        // Timeout 2 mins
        SseEmitter emitter = new SseEmitter(120000L);
        ragChatService.streamChat(request, emitter);
        return emitter;
    }
}
