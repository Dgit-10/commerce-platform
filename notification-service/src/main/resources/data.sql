INSERT INTO notifications
    ( user_id, type, message, recipient, status, created_at)
VALUES
    (
        1,
        'WELCOME_EMAIL',
        'Welcome to our store, John Doe! Your account has been created.',
        'john.doe@example.com',
        'SENT',
        CURRENT_TIMESTAMP
    );

INSERT INTO notifications
    ( user_id, type, message, recipient, status, created_at)
VALUES
    (
        1,
        'ORDER_CONFIRMATION',
        'Your order #1 has been received and total amount is $105.49. Awaiting payment.',
        'john.doe@example.com',
        'SENT',
        CURRENT_TIMESTAMP
    );

INSERT INTO notifications
    ( user_id, type, message, recipient, status, created_at)
VALUES
    (
        1,
        'PAYMENT_SUCCESS',
        'Payment for Order #1 was successful. Transaction ID: TXN_SEED_001',
        'john.doe@example.com',
        'SENT',
        CURRENT_TIMESTAMP
    );

INSERT INTO notifications
    (user_id, type, message, recipient, status, created_at)
VALUES
    (
        1,
        'PAYMENT_FAILURE',
        'Payment failed for Order #3. Reason: Card declined',
        'john.doe@example.com',
        'SENT',
        CURRENT_TIMESTAMP
    );