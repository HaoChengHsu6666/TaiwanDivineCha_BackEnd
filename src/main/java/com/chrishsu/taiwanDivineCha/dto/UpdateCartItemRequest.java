package com.chrishsu.taiwanDivineCha.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemRequest {

    @NotBlank
    private String productId;

    @NotNull
    @Min(0) // 允許數量為0，表示刪除該商品
    private Integer quantity;
}
