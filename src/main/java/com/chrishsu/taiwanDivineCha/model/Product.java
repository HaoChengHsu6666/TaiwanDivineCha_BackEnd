package com.chrishsu.taiwanDivineCha.model;

import com.chrishsu.taiwanDivineCha.constant.ProductCategory;
import com.chrishsu.taiwanDivineCha.converter.StringListConverter; // 引入自定義轉換器

import jakarta.persistence.*; // 使用 jakarta.persistence.*
import lombok.Data;

import java.time.LocalDateTime; // 推薦使用 java.time.* 而不是 java.util.Date
import java.util.List; // 用於 detailImages 和 features

@Data
@Entity
@Table(name = "products") // 確保表名為 products
public class Product {

  @Id // 設定為主鍵
  @Column(name = "id", nullable = false, unique = true, length = 255) // 例如 'sanxia-biluochun'
  private String id; // 與前端的 id: string 保持一致，作為主鍵

  @Column(name = "name", nullable = false) // 商品名稱
  private String name; // 將 ProductName 修改為 name

  @Enumerated(EnumType.STRING) // 將 Enum 存儲為字符串
  @Column(name = "category", nullable = false)
  private ProductCategory category; // 茶品分類

  @Column(name = "image_url", nullable = false) // 商品主要圖片路徑
  private String imageUrl;

  @Convert(converter = StringListConverter.class) // 使用轉換器處理 List<String> 到 String (JSON)
  @Column(name = "detail_images", columnDefinition = "TEXT") // 細節圖片路徑陣列，存儲為 TEXT
  private List<String> detailImages;

  @Column(name = "description", columnDefinition = "TEXT") // 詳細描述，存儲為 TEXT
  private String description;

  @Column(name = "price", nullable = false)
  private Integer price; // 價格

  @Column(name = "stock", nullable = false)
  private Integer stock; // 庫存 (後端特定字段)

  @Column(name = "weight") // 重量 (可選，允許為 NULL)
  private String weight;

  @Convert(converter = StringListConverter.class) // 使用轉換器處理 List<String> 到 String (JSON)
  @Column(name = "features", columnDefinition = "TEXT") // 特點 (可選，允許為 NULL)，存儲為 TEXT
  private List<String> features;

  @Column(name = "origin") // 產地 (可選，允許為 NULL)
  private String origin;

  @Column(name = "created_date", nullable = false, updatable = false)
  private LocalDateTime createdDate; // 創建日期 (使用 LocalDateTime)

  @Column(name = "last_modified_date", nullable = false)
  private LocalDateTime lastModifiedDate; // 最後修改日期 (使用 LocalDateTime)

  // 在實體生命週期事件中自動設定日期
  // createdDate 通常不提供 setter，因為它在創建時自動設定
  @PrePersist
  protected void onCreate() {
    this.createdDate = LocalDateTime.now();
    this.lastModifiedDate = LocalDateTime.now();
  }

  // lastModifiedDate 通常不提供 setter，因為它在更新時自動設定
  @PreUpdate
  protected void onUpdate() {
    this.lastModifiedDate = LocalDateTime.now();
  }

  // Constructor (可以根據需要添加，或使用 Lombok 自動生成)
  public Product() {}

}
