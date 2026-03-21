package com.example.novawear.controller;

import com.example.novawear.dto.FlashSaleDto;
import com.example.novawear.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flash-sales")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    @GetMapping("/active")
    public ResponseEntity<List<FlashSaleDto>> getActive() {
        return ResponseEntity.ok(flashSaleService.findActiveNow());
    }
}
