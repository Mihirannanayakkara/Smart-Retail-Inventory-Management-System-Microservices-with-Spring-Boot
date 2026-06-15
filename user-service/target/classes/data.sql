-- Insert default admin user (password: admin123 - BCrypt encoded)
INSERT INTO users (name, email, password, role, status, created_at)
VALUES ('Admin User', 'admin@smartretail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'ACTIVE', CURRENT_TIMESTAMP);

INSERT INTO users (name, email, password, role, status, created_at)
VALUES ('Staff User', 'staff@smartretail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'STAFF', 'ACTIVE', CURRENT_TIMESTAMP);
