INSERT INTO CUSTOMER (ID, FIRST_NAME, LAST_NAME, EMAIL, PERSONAL_ID_NUMBER, PHONE_NUMBER, CREATED, UPDATED)
VALUES
    (nextval('customer_id_seq'), 'John', 'Doe', 'john.doe@example.com', '12345678901', '+4712345678', NOW(), NOW()),
    (nextval('customer_id_seq'), 'Jane', 'Smith', 'jane.smith@example.com', '98765432109', '+4787654321', NOW(), NOW());

ALTER SEQUENCE customer_id_seq RESTART WITH 100;
