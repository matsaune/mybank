-- Test data for CustomerServiceIntegrationTest
INSERT INTO CUSTOMER (id, first_name, last_name, email, personal_id_number, phone_number, created, updated)
VALUES
    (100, 'Test', 'Customer1', 'test1@example.com', '10000000001', '+4711111111', NOW(), NOW()),
    (101, 'Test', 'Customer2', 'test2@example.com', '10000000002', '+4722222222', NOW(), NOW()),
    (102, 'Test', 'Customer3', 'test3@example.com', '10000000003', '+4733333333', NOW(), NOW());
