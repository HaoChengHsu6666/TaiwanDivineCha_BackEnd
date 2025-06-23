package com.chrishsu.taiwanDivineCha.mapper;

import com.chrishsu.taiwanDivineCha.dto.ProductDto;
import com.chrishsu.taiwanDivineCha.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setCategory(product.getCategory());
        dto.setImageUrl(product.getImageUrl());
        dto.setDetailImages(product.getDetailImages()); // Converter 會自動處理 JSON 轉換
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setWeight(product.getWeight());
        dto.setFeatures(product.getFeatures()); // Converter 會自動處理 JSON 轉換
        dto.setOrigin(product.getOrigin());
        // createdDate 和 lastModifiedDate 可以在某些情況下包含在響應 DTO 中
        return dto;
    }

    public Product toEntity(ProductDto dto) {
        if (dto == null) {
            return null;
        }
        Product product = new Product();
        product.setId(dto.getId()); // 注意：對於新增操作，id 可能需要特殊處理
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());
        product.setDetailImages(dto.getDetailImages());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setWeight(dto.getWeight());
        product.setFeatures(dto.getFeatures());
        product.setOrigin(dto.getOrigin());
        // createdDate 和 lastModifiedDate 不應該從 DTO 設置，讓 JPA @PrePersist/@PreUpdate 自動處理
        return product;
    }

    // 當更新時，可能需要將 DTO 的值複製到現有實體
    public void updateEntityFromDto(ProductDto dto, Product product) {
        if (dto == null || product == null) {
            return;
        }
        // id 通常不更新，除非有特殊需求
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());
        product.setDetailImages(dto.getDetailImages());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setWeight(dto.getWeight());
        product.setFeatures(dto.getFeatures());
        product.setOrigin(dto.getOrigin());
        // lastModifiedDate 會自動更新
    }
}