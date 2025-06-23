package com.chrishsu.taiwanDivineCha.repository;

import com.chrishsu.taiwanDivineCha.constant.ProductCategory;
import com.chrishsu.taiwanDivineCha.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// @Repository 註解是可選的，因為 JpaRepository 已經包含其功能，但加上它會讓意圖更明確。
@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    // JpaRepository 提供了基本的 CRUD 操作：
    // - save(T entity): 保存或更新實體
    // - findById(ID id): 根據ID查找實體，返回 Optional<T>
    // - findAll(): 查找所有實體
    // - deleteById(ID id): 根據ID刪除實體
    // - existsById(ID id): 檢查實體是否存在

    // 您可以定義額外的查詢方法。Spring Data JPA 會根據方法名自動生成查詢邏輯。
    // 範例：根據產品名稱查找 (Spring Data JPA 會自動推斷 SQL: SELECT * FROM products WHERE name = ?)
    List<Product> findByName(String name);

    // 範例：根據茶品分類查找 (Spring Data JPA 會自動推斷 SQL: SELECT * FROM products WHERE category = ?)
    // 注意：這裡 TeaProductCategory 會自動通過 @Enumerated(EnumType.STRING) 映射到資料庫的字符串
    List<Product> findByCategory(ProductCategory category);

    // 範例：根據名稱模糊查詢，忽略大小寫 (使用 CONTAINS 或 LIKE)
    List<Product> findByNameContainingIgnoreCase(String name);

    // 根據您前端的 `id` 是 `string` 類型，我們這裡的第二個泛型參數也應為 `String`。
    // 如果您將來決定 `id` 是一個自動增長的 `Long` 類型，這裡需要改為 `JpaRepository<Product, Long>`。
}