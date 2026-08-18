-- Order 1
INSERT INTO orders
    (id, user_id, status, total_amount, created_at)
VALUES
    (1, 1, 'PAID', 105.49, CURRENT_TIMESTAMP);

-- Order 1 items
INSERT INTO order_items
    (order_id, product_id, quantity, price)
VALUES
    (1, 1, 1, 25.50),
    (1, 2, 1, 79.99);

-- Order 2
INSERT INTO orders
    (id, user_id, status, total_amount, created_at)
VALUES
    (2, 1, 'PENDING', 70.00, CURRENT_TIMESTAMP);

INSERT INTO order_items
    (order_id, product_id, quantity, price)
VALUES
    (2, 3, 2, 35.00);

-- Order 3
INSERT INTO orders
    (id, user_id, status, total_amount, created_at)
VALUES
    (3, 1, 'DELIVERED', 25.50, CURRENT_TIMESTAMP);

INSERT INTO order_items
    (order_id, product_id, quantity, price)
VALUES
    (3, 1, 1, 25.50);