-- Clear existing test data
TRUNCATE TABLE CUSTOMER CASCADE;

-- Insert test customers
INSERT INTO CUSTOMER (ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER, PERSONAL_ID_NUMBER, CREATED, UPDATED)
VALUES
    (1, 'Test', 'User', 'test.user@example.com', '+4711223344', '12345678901', NOW(), NOW()),
    (2, 'Jane', 'Smith', 'jane.smith@example.com', '+4755667788', '23456789012', NOW(), NOW()),
    (3, 'Bob', 'Johnson', 'bob.johnson@example.com', '+4799887766', '34567890123', NOW(), NOW());

-- Reset the sequence
ALTER SEQUENCE customer_id_seq RESTART WITH 4;
