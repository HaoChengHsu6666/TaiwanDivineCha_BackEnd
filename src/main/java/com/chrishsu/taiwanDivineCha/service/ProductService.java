package com.chrishsu.taiwanDivineCha.service;

import com.chrishsu.taiwanDivineCha.dto.ProductDto;

import java.util.List;

public interface ProductService {

    List<ProductDto> getAllProducts();

    ProductDto getProductById(String id);

    ProductDto createProduct(ProductDto productDto);

    ProductDto updateProduct(String id, ProductDto productDto);

    void deleteProduct(String id);

    List<ProductDto> getProductsByCategory(String categoryDisplayName);
}
