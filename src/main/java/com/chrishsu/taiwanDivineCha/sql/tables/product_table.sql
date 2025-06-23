-- 為了清晰起見，我們假設stock預設為100，日期為當前時間。
-- 如果您的數據庫有不同的日期時間函數，請自行調整。
-- 例如：MySQL 是 NOW()，PostgreSQL 是 NOW() 或 CURRENT_TIMESTAMP。

-- 產品 1: 三峽碧螺春綠
INSERT INTO products (
    id, name, category, image_url, detail_images, description,
    price, stock, weight, features, origin, created_date, last_modified_date
) VALUES (
    'sanxia-biluochun',
    '三峽碧螺春綠',
    'GREEN_TEA', -- 對應 TeaProductCategory.GREEN_TEA
    'assets/products/sanxia-biluochun.jpg',
    '[]', -- detailImages 為空陣列
    '這是一款來自三峽的頂級碧螺春綠茶，茶葉鮮嫩，香氣清雅，口感甘醇。',
    800,
    100, -- 預設庫存
    '150g',
    '["手工採摘", "清香甘醇", "高山綠茶"]', -- features JSON 字符串
    '台灣三峽',
    NOW(), -- 當前日期時間
    NOW()  -- 當前日期時間
);

-- 產品 2: 杉林溪青
INSERT INTO products (
    id, name, category, image_url, detail_images, description,
    price, stock, weight, features, origin, created_date, last_modified_date
) VALUES (
    'shanlinxi-qing',
    '杉林溪青',
    'OOLONG_TEA', -- 對應 TeaProductCategory.OOLONG_TEA
    'assets/products/shanlinxi-qing.jpg',
    '[]',
    '杉林溪高山烏龍茶，茶湯金黃明亮，帶有獨特的花果香，喉韻悠長。',
    1200,
    100,
    '150g',
    '["高山烏龍", "花果香", "喉韻悠長"]',
    '台灣杉林溪',
    NOW(),
    NOW()
);

-- 產品 3: 凍頂烏龍
INSERT INTO products (
    id, name, category, image_url, detail_images, description,
    price, stock, weight, features, origin, created_date, last_modified_date
) VALUES (
    'dongding-oolong',
    '凍頂烏龍',
    'OOLONG_TEA', -- 對應 TeaProductCategory.OOLONG_TEA
    'assets/products/dongding-oolong.jpg',
    '[]',
    '經典凍頂烏龍，茶葉緊實，發酵程度適中，香氣濃郁，口感醇厚回甘。',
    950,
    100,
    '150g',
    '["傳統工藝", "濃郁醇厚", "回甘"]',
    '台灣鹿谷',
    NOW(),
    NOW()
);

-- 產品 4: 北埔東方美人
INSERT INTO products (
    id, name, category, image_url, detail_images, description,
    price, stock, weight, features, origin, created_date, last_modified_date
) VALUES (
    'beipu-oriental-beauty',
    '北埔東方美人',
    'BLACK_TEA', -- 對應 TeaProductCategory.BLACK_TEA
    'assets/products/beipu-oriental-beauty.jpg',
    '[]',
    '獨具蜜香和熟果香的東方美人茶，茶湯橙紅，滋味醇厚甘潤。',
    1500,
    100,
    '75g',
    '["小綠葉蟬", "蜜香", "熟果香"]',
    '台灣北埔',
    NOW(),
    NOW()
);

-- 產品 5: 日月潭紅玉
INSERT INTO products (
    id, name, category, image_url, detail_images, description,
    price, stock, weight, features, origin, created_date, last_modified_date
) VALUES (
    'sun-moon-lake-ruby',
    '日月潭紅玉',
    'BLACK_TEA', -- 對應 TeaProductCategory.BLACK_TEA
    'assets/products/sun-moon-lake-ruby.jpg',
    '[]',
    '台灣特有紅茶品種，帶有天然肉桂和薄荷香氣，茶湯紅艷，口感溫潤。',
    700,
    100,
    '100g',
    '["台茶18號", "肉桂薄荷香", "溫潤"]',
    '台灣日月潭',
    NOW(),
    NOW()
);

-- 產品 6: 木柵鐵觀音
INSERT INTO products (
    id, name, category, image_url, detail_images, description,
    price, stock, weight, features, origin, created_date, last_modified_date
) VALUES (
    'Mucha_Tieguanyin',
    '木柵鐵觀音',
    'OOLONG_TEA', -- 對應 TeaProductCategory.OOLONG_TEA
    'assets/products/Mucha_Tieguanyin.jpg',
    '[]',
    '傳統木柵鐵觀音，經過重發酵和足夠的烘焙，茶湯醇厚，帶有獨特的火香和觀音韻。',
    1000,
    100,
    '150g',
    '["傳統工藝", "濃郁焙火香", "觀音韻"]',
    '台灣木柵',
    NOW(),
    NOW()
);

-- 產品 7: 阿里山金萱
INSERT INTO products (
    id, name, category, image_url, detail_images, description,
    price, stock, weight, features, origin, created_date, last_modified_date
) VALUES (
    'Alishan_Jinxuan_Tea',
    '阿里山金萱',
    'OOLONG_TEA', -- 金萱通常歸類為青茶或烏龍茶，這裡根據您的前端分類選擇 '青茶' 映射為 OOLONG_TEA。如果您的青茶特指輕發酵烏龍，那這樣可以。
    'assets/products/Alishan_Jinxuan_Tea.jpg',
    '[]',
    '來自阿里山高海拔的金萱茶，茶湯清澈，帶有獨特的奶香味和淡淡的花香，口感滑潤甘甜。',
    900,
    100,
    '150g',
    '["高山金萱", "天然奶香", "清甜滑潤"]',
    '台灣阿里山',
    NOW(),
    NOW()
);

-- 產品 8: 梨山烏龍綠
INSERT INTO products (
    id, name, category, image_url, detail_images, description,
    price, stock, weight, features, origin, created_date, last_modified_date
) VALUES (
    'Lishan_Green_Tea',
    '梨山烏龍綠',
    'GREEN_TEA', -- 如果您定義的綠茶包含這種輕發酵烏龍綠，則為 GREEN_TEA
    'assets/products/Lishan_Green_Tea.jpg',
    '[]',
    '梨山烏龍綠，結合了綠茶的鮮爽和烏龍茶的醇厚，口感清雅回甘，帶有高山獨有的冷礦味。',
    1300,
    100,
    '150g',
    '["高山茶", "清雅回甘", "冷礦味"]',
    '台灣梨山',
    NOW(),
    NOW()
);