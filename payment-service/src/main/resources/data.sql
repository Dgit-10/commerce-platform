-- Successful payment for Order 1
INSERT INTO payments
    (id, order_id, user_id, amount, status, transaction_id, failure_reason, created_at)
VALUES
    (1, 1, 1, 105.49, 'SUCCESS', 'TXN_SEED_001', NULL, CURRENT_TIMESTAMP);


-- Pending payment for Order 2
INSERT INTO payments
    (id, order_id, user_id, amount, status, transaction_id, failure_reason, created_at)
VALUES
    (2, 2, 1, 70.00, 'PENDING', NULL, NULL, CURRENT_TIMESTAMP);


-- Failed payment for Order 3
INSERT INTO payments
    (id, order_id, user_id, amount, status, transaction_id, failure_reason, created_at)
VALUES
    (3, 3, 1, 25.50, 'FAILED', NULL, 'Card declined', CURRENT_TIMESTAMP);