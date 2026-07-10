use mohsin_studio ;
-- ============================================
-- MOHSIN STUDIO - DATABASE SCHEMA
-- Best-effort reconstruction based on code/models seen in this conversation.
-- users, categories, login_otps, audit_logs = CONFIRMED (describe se liye gaye)
-- customers, packages, orders, sales_entries, payments, expenses = APPROX (code se guess)
-- Agar koi column missing/mismatch ho to bata dena, theek kar denge.
-- ============================================

CREATE TABLE IF NOT EXISTS users (
    id INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','CEO','CO_FOUNDER','CLERK') NOT NULL,
    email VARCHAR(100) NOT NULL,
    profile_image_path VARCHAR(255) NULL,
    phone VARCHAR(20) NULL,
    profile_picture_path VARCHAR(500) NULL,
    is_active TINYINT(1) DEFAULT 1,
    is_locked TINYINT(1) DEFAULT 0,
    login_attempts INT(11) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS login_otps (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    otp_code    VARCHAR(6) NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    used        BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_table VARCHAR(50) NULL,
    target_id INT NULL,
    details VARCHAR(500) NULL,
    ip_address VARCHAR(50) NULL,
    `timestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    KEY (user_id),
    KEY (`timestamp`)
);

-- ===== APPROX (verify against real Windows/Linux DB) =====

CREATE TABLE IF NOT EXISTS customers (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NULL,
    address VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS packages (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    package_name VARCHAR(150) NOT NULL,
    description VARCHAR(500) NULL,
    price DOUBLE NOT NULL,
    category VARCHAR(50) NULL,
    services VARCHAR(500) NULL,
    discount DOUBLE DEFAULT 0,
    is_active TINYINT(1) DEFAULT 1,
    created_by INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS orders (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    package_id INT NULL,
    event_date DATE NULL,
    days INT DEFAULT 1,
    total_amount DOUBLE NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING_APPROVAL',
    created_by INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (package_id) REFERENCES packages(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);
ALTER TABLE orders ADD COLUMN order_type    VARCHAR(200)  NULL DEFAULT NULL;
ALTER TABLE orders ADD COLUMN amount        DECIMAL(10,2) NULL DEFAULT NULL;
ALTER TABLE orders ADD COLUMN order_date    DATE          NULL DEFAULT NULL;
ALTER TABLE orders ADD COLUMN approved_at   DATETIME      NULL DEFAULT NULL;
ALTER TABLE orders ADD COLUMN notes         TEXT          NULL DEFAULT NULL;
ALTER TABLE orders ADD COLUMN delivery_date DATE          NULL DEFAULT NULL;
ALTER TABLE orders ADD COLUMN approved_by   INT           NULL DEFAULT NULL;




CREATE TABLE IF NOT EXISTS payments (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    amount DOUBLE NOT NULL,
    payment_method VARCHAR(30) NULL,
    transaction_id VARCHAR(100) NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);
ALTER TABLE payments ADD COLUMN advance_paid  DECIMAL(10,2) NULL DEFAULT 0.00;
ALTER TABLE payments ADD COLUMN total_amount  DECIMAL(10,2) NULL DEFAULT 0.00;
ALTER TABLE payments ADD COLUMN recorded_by   INT           NULL DEFAULT NULL;
ALTER TABLE payments ADD COLUMN created_at    TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP;







CREATE TABLE IF NOT EXISTS sales_entries (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    entry_type VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    amount DOUBLE NOT NULL,
    entered_by INT NOT NULL,
    entry_date DATE NOT NULL,
    notes VARCHAR(255) NULL,
    FOREIGN KEY (entered_by) REFERENCES users(id)
);
ALTER TABLE sales_entries ADD COLUMN created_at DATETIME NULL DEFAULT NULL;




CREATE TABLE IF NOT EXISTS expenses (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount DOUBLE NOT NULL,
    expense_date DATE NOT NULL,
    added_by INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (added_by) REFERENCES users(id)
);
ALTER TABLE expenses ADD COLUMN category    VARCHAR(100) NULL DEFAULT NULL;
ALTER TABLE expenses ADD COLUMN recorded_by INT          NULL DEFAULT NULL;
ALTER TABLE expenses ADD COLUMN notes       TEXT         NULL DEFAULT NULL;




CREATE TABLE IF NOT EXISTS daily_reports (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    report_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS customer_drives (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    drive_name VARCHAR(100) NOT NULL,
    location VARCHAR(150),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);






























use mohsin_studio;
-- show tables;
-- show table status;

-- describe audit_logs;
-- describe categories;
-- describe customers;
-- describe customer_drives;
-- describe daily_reports;
-- describe expenses;
-- describe login_otps;
-- describe orders;
-- describe packages;
-- describe payments;
-- describe sales_entries;
-- describe  users;



-- Foreign key checks band karo
SET FOREIGN_KEY_CHECKS = 0;

-- Saari tables truncate karo
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE categories;
TRUNCATE TABLE customers;
TRUNCATE TABLE customer_drives;
TRUNCATE TABLE daily_reports;
TRUNCATE TABLE expenses;
TRUNCATE TABLE login_otps;
TRUNCATE TABLE orders;
TRUNCATE TABLE packages;
TRUNCATE TABLE payments;
TRUNCATE TABLE sales_entries;

-- Foreign key checks wapas on karo
SET FOREIGN_KEY_CHECKS = 1;

















