INSERT INTO users (email, password_hash, full_name, phone_number, address, created_at)
VALUES ('john.doe@example.com', 'hashed_password_123', 'John Doe', '+1234567890', '123 Main St, NY', CURRENT_TIMESTAMP);


INSERT INTO users
    (email, password_hash, full_name, phone_number, address, created_at)
VALUES
    ('jane.smith@example.com',
     'hashed_password_456',
     'Jane Smith',
     '+1987654321',
     '456 Park Ave, NY',
     CURRENT_TIMESTAMP);

INSERT INTO users
    (email, password_hash, full_name, phone_number, address, created_at)
VALUES
    ('alex.wilson@example.com',
     'hashed_password_789',
     'Alex Wilson',
     '+1555123456',
     '789 Broadway, NY',
     CURRENT_TIMESTAMP);