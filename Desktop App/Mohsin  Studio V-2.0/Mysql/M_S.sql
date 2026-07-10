-- create database mohsin_studio;
use mohsin_studio;
describe customers;




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






CREATE TABLE audit_logs (
    id          INT          NOT NULL AUTO_INCREMENT,
    user_id     INT          NOT NULL,
    action      VARCHAR(100) NOT NULL,
    target_table VARCHAR(50) NULL DEFAULT NULL,
    target_id   INT          NULL DEFAULT NULL,
    details     VARCHAR(500) NULL DEFAULT NULL,
    ip_address  VARCHAR(50)  NULL DEFAULT NULL,
    timestamp   TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY (user_id),
    KEY (timestamp)
);



CREATE TABLE categories (
    id        INT          NOT NULL AUTO_INCREMENT,
    name      VARCHAR(100) NOT NULL,
    type      VARCHAR(20)  NOT NULL,
    is_active TINYINT(1)   NULL DEFAULT 1,

    PRIMARY KEY (id)
);


CREATE TABLE customers (
    id         INT          NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NULL DEFAULT NULL,
    phone      VARCHAR(20)  NOT NULL,
    email      VARCHAR(100) NULL DEFAULT NULL,
    address    VARCHAR(255) NULL DEFAULT NULL,
    created_at TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id)
);


CREATE TABLE customer_drives (
    id          INT          NOT NULL AUTO_INCREMENT,
    customer_id INT          NOT NULL,
    drive_name  VARCHAR(100) NOT NULL,
    location    VARCHAR(150) NULL DEFAULT NULL,
    notes       TEXT         NULL DEFAULT NULL,
    created_at  TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY (customer_id),
    
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);


CREATE TABLE daily_reports (
    id          INT       NOT NULL AUTO_INCREMENT,
    report_date DATE      NOT NULL,
    created_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id)
);


CREATE TABLE expenses (
    id           INT          NOT NULL AUTO_INCREMENT,
    description  VARCHAR(255) NOT NULL,
    amount       DOUBLE       NOT NULL,
    expense_date DATE         NOT NULL,
    added_by     INT          NULL DEFAULT NULL,
    created_at   TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP,
    category     VARCHAR(100) NULL DEFAULT NULL,
    recorded_by  INT          NULL DEFAULT NULL,
    notes        TEXT         NULL DEFAULT NULL,

    PRIMARY KEY (id),
    KEY (added_by)
);


CREATE TABLE  login_otps (
    id         INT         NOT NULL AUTO_INCREMENT,
    user_id    INT         NOT NULL,
    otp_code   VARCHAR(6)  NOT NULL,
    expires_at TIMESTAMP   NOT NULL,
    used       TINYINT(1)  NULL DEFAULT 0,
    created_at TIMESTAMP   NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY (user_id)
);


CREATE TABLE orders (
    id            INT           NOT NULL AUTO_INCREMENT,
    customer_id   INT           NOT NULL,
    package_id    INT           NULL DEFAULT NULL,
    order_type    VARCHAR(200)  NULL DEFAULT NULL,
    event_date    DATE          NULL DEFAULT NULL,
    days          INT           NULL DEFAULT 1,
    amount        DECIMAL(10,2) NULL DEFAULT NULL,
    order_date    DATE          NULL DEFAULT NULL,
    status        VARCHAR(30)   NULL DEFAULT 'PENDING_APPROVAL',
    created_by    INT           NULL DEFAULT NULL,
    created_at    TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at   DATETIME      NULL DEFAULT NULL,
    notes         TEXT          NULL DEFAULT NULL,
    delivery_date DATE          NULL DEFAULT NULL,
    approved_by   INT           NULL DEFAULT NULL,

    PRIMARY KEY (id),
    KEY (customer_id),
    KEY (package_id),
    KEY (created_by)
);



CREATE TABLE packages (
    id           INT          NOT NULL AUTO_INCREMENT,
    package_name VARCHAR(150) NOT NULL,
    description  VARCHAR(500) NULL DEFAULT NULL,
    price        DOUBLE       NOT NULL,
    category     VARCHAR(50)  NULL DEFAULT NULL,
    services     VARCHAR(50)  NULL DEFAULT NULL,
    discount     DOUBLE       NULL DEFAULT 0,
    is_active    TINYINT(1)   NULL DEFAULT 1,
    created_by   INT          NULL DEFAULT NULL,
    created_at   TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY (created_by)
);



CREATE TABLE payments (
    id              INT           NOT NULL AUTO_INCREMENT,
    order_id        INT           NOT NULL,
    amount          DOUBLE        NOT NULL,
    payment_method  VARCHAR(30)   NULL DEFAULT NULL,
    transaction_id  VARCHAR(100)  NULL DEFAULT NULL,
    payment_date    TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP,
    advance_paid    DECIMAL(10,2) NULL DEFAULT 0.00,
    total_amount    DECIMAL(10,2) NULL DEFAULT 0.00,
    recorded_by     INT           NULL DEFAULT NULL,
    created_at      TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY (order_id)
);


CREATE TABLE sales_entries (
    id          INT          NOT NULL AUTO_INCREMENT,
    entry_type  VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL DEFAULT NULL,
    amount      DOUBLE       NOT NULL,
    entered_by  INT          NOT NULL,
    created_at  DATETIME     NULL DEFAULT NULL,
    entry_date  DATETIME     NULL DEFAULT NULL,
    notes       VARCHAR(255) NULL DEFAULT NULL,

    PRIMARY KEY (id),
    KEY (entered_by)
);


CREATE TABLE users (
    id                  INT                                         NOT NULL AUTO_INCREMENT,
    full_name           VARCHAR(100)                               NOT NULL,
    username            VARCHAR(50)                                NOT NULL,
    password_hash       VARCHAR(255)                               NOT NULL,
    role                ENUM('ADMIN','CEO','CO_FOUNDER','CLERK')   NOT NULL,
    email               VARCHAR(100)                               NOT NULL,
    profile_image_path  VARCHAR(255)                               NULL DEFAULT NULL,
    phone               VARCHAR(20)                                NULL DEFAULT NULL,
    profile_picture_path VARCHAR(500)                              NULL DEFAULT NULL,
    is_active           TINYINT(1)                                 NULL DEFAULT 1,
    is_locked           TINYINT(1)                                 NULL DEFAULT 0,
    login_attempts      INT                                        NULL DEFAULT 0,
    created_at          TIMESTAMP                                  NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP                                  NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY (username)
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

