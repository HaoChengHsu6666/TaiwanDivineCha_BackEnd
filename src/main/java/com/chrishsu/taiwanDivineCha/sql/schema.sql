// database
use taiwan_divine_cha

// users
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- 考慮加密後長度，可能需要更大
    reset_password_token VARCHAR(255) UNIQUE, -- 考慮 token 長度
    reset_password_expires DATETIME,
    create_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_reset_password_token (reset_password_token)
);

// products
CREATE TABLE products (
    id VARCHAR(255) PRIMARY KEY, -- 商品唯一識別碼 (例如 'sanxia-biluochun')
    name VARCHAR(255) NOT NULL, -- 商品名稱
    category VARCHAR(50) NOT NULL, -- 茶品分類 (儲存 Enum 的字符串值)
    image_url VARCHAR(255) NOT NULL, -- 商品主要圖片路徑
    detail_images TEXT, -- 商品細節圖片路徑陣列 (JSON 字符串)
    description TEXT, -- 詳細描述
    price INT NOT NULL, -- 價格
    stock INT NOT NULL, -- 庫存
    weight VARCHAR(100), -- 重量 (可選，允許 NULL)
    features TEXT, -- 特點 (JSON 字符串，可選，允許 NULL)
    origin VARCHAR(100), -- 產地 (可選，允許 NULL)
    created_date DATETIME NOT NULL, -- 創建日期
    last_modified_date DATETIME NOT NULL -- 最後修改日期
);
