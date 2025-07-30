package com.chrishsu.taiwanDivineCha.service;

import com.chrishsu.taiwanDivineCha.dto.AddToCartRequest;
import com.chrishsu.taiwanDivineCha.dto.CartDto;
import com.chrishsu.taiwanDivineCha.dto.UpdateCartItemRequest;
import com.chrishsu.taiwanDivineCha.model.User;
import com.chrishsu.taiwanDivineCha.model.Cart;

public interface CartService {
    Cart getOrCreateCart(User user);
    void addToCart(User user, AddToCartRequest addToCartRequest);
    void updateCartItem(User user, UpdateCartItemRequest updateCartItemRequest);
    void removeCartItem(User user, String productId, Integer weight);
    CartDto getCart(User user);
}