DROP TABLE IF EXISTS sample_order;

CREATE TABLE sample_order (
    id VARCHAR(64) PRIMARY KEY,
    product_code VARCHAR(128) NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL
);
