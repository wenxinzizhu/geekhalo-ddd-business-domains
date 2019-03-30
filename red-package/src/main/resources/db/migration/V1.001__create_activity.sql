CREATE TABLE tb_red_package_activity
(
    `_id` bigint PRIMARY KEY AUTO_INCREMENT,
    id char(64) NOT NULL,
    total_amount int NOT NULL,
    total_number int NOT NULL,
    `version` int NOT NULL,
    surplus_amount int NOT NULL,
    surplus_number int NOT NULL
);
CREATE UNIQUE INDEX id ON tb_red_package_activity (id);