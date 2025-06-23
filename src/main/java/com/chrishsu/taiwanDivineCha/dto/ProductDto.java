package com.chrishsu.taiwanDivineCha.dto;

import com.chrishsu.taiwanDivineCha.constant.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;

@Data
public class ProductDto {

    // 對於創建產品，id 可能由後端生成或由前端提供
    // 如果 id 是業務識別碼，創建時前端可能提供
    // 如果是自動增長的數字ID，則創建時無需提供，更新時提供
    // 假設創建時前端需要提供一個業務ID
    @NotBlank(message = "Product ID cannot be blank")
    private String id;

    @NotBlank(message = "Product name cannot be blank")
    private String name;

    @NotNull(message = "Category cannot be null")
    private ProductCategory category; // 使用 Enum 類型

    @NotBlank(message = "Image URL cannot be blank")
    private String imageUrl;

    private List<String> detailImages; // 前端傳送 List<String>

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotNull(message = "Price cannot be null")
    @PositiveOrZero(message = "Price must be positive or zero")
    private Integer price;

    @NotNull(message = "Stock cannot be null")
    @PositiveOrZero(message = "Stock must be positive or zero")
    private Integer stock;

    private String weight;
    private List<String> features; // 前端傳送 List<String>
    private String origin;

    // 後端內部使用的時間戳，通常不在DTO中接收，但可以在返回時包含
    // private LocalDateTime createdDate;
    // private LocalDateTime lastModifiedDate;

    public ProductDto() {}

    public ProductDto(String id, String name, ProductCategory category, String imageUrl, List<String> detailImages, String description, Integer price, Integer stock, String weight, List<String> features, String origin) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
        this.detailImages = detailImages;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.weight = weight;
        this.features = features;
        this.origin = origin;
    }
}