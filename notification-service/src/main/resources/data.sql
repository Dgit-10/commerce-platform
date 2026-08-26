INSERT INTO notifications
    (id, user_id, type, message, recipient, status, created_at)
VALUES
    (
        1,
        1,
        'WELCOME_EMAIL',
        'Welcome to our store, John Doe! Your account has been created.',
        'john.doe@example.com',
        'SENT',
        CURRENT_TIMESTAMP
    );

INSERT INTO notifications
    (id, user_id, type, message, recipient, status, created_at)
VALUES
    (
        2,
        1,
        'ORDER_CONFIRMATION',
        'Your order #1 has been received and total amount is $105.49. Awaiting payment.',
        'john.doe@example.com',
        'SENT',
        CURRENT_TIMESTAMP
    );

INSERT INTO notifications
    (id, user_id, type, message, recipient, status, created_at)
VALUES
    (
        3,
        1,
        'PAYMENT_SUCCESS',
        'Payment for Order #1 was successful. Transaction ID: TXN_SEED_001',
        'john.doe@example.com',
        'SENT',
        CURRENT_TIMESTAMP
    );

INSERT INTO notifications
    (id, user_id, type, message, recipient, status, created_at)
VALUES
    (
        4,
        1,
        'PAYMENT_FAILURE',
        'Payment failed for Order #3. Reason: Card declined',
        'john.doe@example.com',
        'SENT',
        CURRENT_TIMESTAMP
    );