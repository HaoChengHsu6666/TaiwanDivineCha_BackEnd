CREATE TABLE contact_us (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inquiry_item VARCHAR(255) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    estimated_quantity INT,
    budget_per_unit VARCHAR(100),
    delivery_date DATE,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);