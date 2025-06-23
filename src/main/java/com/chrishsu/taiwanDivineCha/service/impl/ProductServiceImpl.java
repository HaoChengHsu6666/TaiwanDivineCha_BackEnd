package com.chrishsu.taiwanDivineCha.service.impl;

import com.chrishsu.taiwanDivineCha.constant.ProductCategory;
import com.chrishsu.taiwanDivineCha.dto.ProductDto;
import com.chrishsu.taiwanDivineCha.exception.ResourceNotFoundException;
import com.chrishsu.taiwanDivineCha.mapper.ProductMapper;
import com.chrishsu.taiwanDivineCha.model.Product;
import com.chrishsu.taiwanDivineCha.repository.ProductRepository;
import com.chrishsu.taiwanDivineCha.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper; // 注入 Mapper

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    // 獲取所有產品
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toDto) // 轉換為 DTO
                .collect(Collectors.toList());
    }

    // 根據 ID 獲取產品
    public ProductDto getProductById(String id) { // ID 類型改為 String
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    // 創建新產品
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        if (productRepository.existsById(productDto.getId())) {
            throw new IllegalArgumentException("Product with ID " + productDto.getId() + " already exists.");
        }
        Product product = productMapper.toEntity(productDto);
        // 通常創建時，DTO 的 ID 會直接用作 Entity 的 ID
        // 如果是自動生成的 ID，則這行會不同
        return productMapper.toDto(productRepository.save(product));
    }

    // 更新產品
    @Transactional
    public ProductDto updateProduct(String id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // 使用 Mapper 更新現有實體
        productMapper.updateEntityFromDto(productDto, existingProduct);

        return productMapper.toDto(productRepository.save(existingProduct));
    }

    // 刪除產品
    @Transactional
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    // 根據分類獲取產品 (假設 TeaProductCategory 有 fromDisplayName 方法)
    public List<ProductDto> getProductsByCategory(String categoryDisplayName) {
        try {
            ProductCategory category = ProductCategory.fromDisplayName(categoryDisplayName);
            return productRepository.findByCategory(category).stream()
                    .map(productMapper::toDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            // 如果分類名稱無效，可以返回空列表或拋出特定異常
            return List.of();
        }
    }
}