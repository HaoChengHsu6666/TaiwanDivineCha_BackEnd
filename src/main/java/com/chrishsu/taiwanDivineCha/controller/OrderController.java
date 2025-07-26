package com.chrishsu.taiwanDivineCha.controller;

import com.chrishsu.taiwanDivineCha.dto.CreateOrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody CreateOrderRequest createOrderRequest) {
        System.out.println("Received order request: " + createOrderRequest);
        // 在這裡處理訂單創建邏輯，例如保存到資料庫
        return ResponseEntity.status(HttpStatus.CREATED).body("Order created successfully!");
    }
}
