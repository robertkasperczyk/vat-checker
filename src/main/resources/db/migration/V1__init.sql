CREATE TABLE vat_check_result
(
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    valid   BOOLEAN      NOT NULL,
    name    VARCHAR(255) NULL,
    address VARCHAR(255) NULL,
    PRIMARY KEY (id)
);

CREATE TABLE vat_check_order
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    submitted  DATETIME    NOT NULL,
    vat_number VARCHAR(32) NOT NULL,
    status     VARCHAR(32) NOT NULL,
    result_id  BIGINT      NULL,
    PRIMARY KEY (id),
    CONSTRAINT vat_check_order_vat_check_result_fk FOREIGN KEY (result_id) REFERENCES vat_check_result (id)
);
