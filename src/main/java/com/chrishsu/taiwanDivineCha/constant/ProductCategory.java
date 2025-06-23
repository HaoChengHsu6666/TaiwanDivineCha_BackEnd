package com.chrishsu.taiwanDivineCha.constant;

public enum ProductCategory {
    GREEN_TEA("綠茶"), // 綠茶
    CHIN_TEA("青茶"), // 青茶
    OOLONG_TEA("烏龍茶"), // 烏龍茶
    BLACK_TEA("紅茶"); // 紅茶


    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // 可選：根據顯示名稱獲取 Enum 實例
    public static ProductCategory fromDisplayName(String displayName) {
        for (ProductCategory category : ProductCategory.values()) {
            if (category.getDisplayName().equalsIgnoreCase(displayName)) {
                return category;
            }
        }
        throw new IllegalArgumentException("No category with display name " + displayName);
    }
}
