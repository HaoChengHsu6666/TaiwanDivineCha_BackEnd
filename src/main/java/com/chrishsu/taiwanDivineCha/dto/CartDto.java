package com.chrishsu.taiwanDivineCha.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartDto {
    private Integer cartId;
    private Integer userId;
    private List<CartItemDto> cartItems;
    private Integer totalAmount;
}
