package com.chrishsu.taiwanDivineCha.controller;

import com.chrishsu.taiwanDivineCha.dto.AddToCartRequest;
import com.chrishsu.taiwanDivineCha.dto.CartDto;
import com.chrishsu.taiwanDivineCha.dto.UpdateCartItemRequest;
import com.chrishsu.taiwanDivineCha.model.User;
import com.chrishsu.taiwanDivineCha.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<Void> addItemToCart(@AuthenticationPrincipal User user, @RequestBody @Valid AddToCartRequest addToCartRequest) {
        cartService.addToCart(user, addToCartRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/items")
    public ResponseEntity<Void> updateCartItemQuantity(@AuthenticationPrincipal User user, @RequestBody @Valid UpdateCartItemRequest updateCartItemRequest) {
        cartService.updateCartItem(user, updateCartItemRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeCartItem(@AuthenticationPrincipal User user, 
                                               @PathVariable String productId, 
                                               @RequestParam Integer weight) {
        cartService.removeCartItem(user, productId, weight);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart(@AuthenticationPrincipal User user) {
        CartDto cartDto = cartService.getCart(user);
        return ResponseEntity.ok(cartDto);
    }
}