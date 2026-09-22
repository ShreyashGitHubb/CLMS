CREATE DATABASE IF NOT EXISTS clms;
USE clms;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'LAB_ASSISTANT', 'STUDENT') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE equipment (
    equipment_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(80) NOT NULL,
    description TEXT,
    total_quantity INT NOT NULL CHECK (total_quantity >= 0),
    available_quantity INT NOT NULL CHECK (available_quantity >= 0),
    location VARCHAR(100) NOT NULL,
    equipment_condition ENUM('GOOD', 'FAIR', 'DAMAGED') NOT NULL DEFAULT 'GOOD',
    status ENUM('AVAILABLE', 'LOW_STOCK', 'MAINTENANCE', 'RETIRED') NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (available_quantity <= total_quantity)
);

CREATE TABLE borrow_transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    equipment_id INT NOT NULL,
    issue_date DATE,
    due_date DATE NOT NULL,
    return_date DATE,
    status ENUM('PENDING', 'APPROVED', 'RETURNED', 'REJECTED', 'OVERDUE') NOT NULL DEFAULT 'PENDING',
    fine_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    approved_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_transaction_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id),
    CONSTRAINT fk_transaction_approver FOREIGN KEY (approved_by) REFERENCES users(user_id)
);

CREATE TABLE maintenance_logs (
    maintenance_id INT PRIMARY KEY AUTO_INCREMENT,
    equipment_id INT NOT NULL,
    reported_by INT NOT NULL,
    issue_description TEXT NOT NULL,
    reported_date DATE NOT NULL,
    repair_date DATE,
    repair_cost DECIMAL(10, 2) DEFAULT 0.00,
    status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED') NOT NULL DEFAULT 'OPEN',
    CONSTRAINT fk_maintenance_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id),
    CONSTRAINT fk_maintenance_reporter FOREIGN KEY (reported_by) REFERENCES users(user_id)
);

CREATE INDEX idx_equipment_status ON equipment(status);
CREATE INDEX idx_transaction_user ON borrow_transactions(user_id);
CREATE INDEX idx_transaction_status ON borrow_transactions(status);
CREATE INDEX idx_maintenance_equipment ON maintenance_logs(equipment_id);
