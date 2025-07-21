package com.chrishsu.taiwanDivineCha.dto;

import com.chrishsu.taiwanDivineCha.constant.ProductCategory;
import lombok.Data;

@Data
public class CartItemDto {
    private String productId;
    private String productName;
    private String imageUrl;
    private ProductCategory category;
    private Integer price;
    private Integer stock;
    private Integer quantity;
    private Integer subtotal;
}
