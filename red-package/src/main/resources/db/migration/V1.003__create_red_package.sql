CREATE TABLE tb_red_package
(
    `_id` bigint PRIMARY KEY AUTO_INCREMENT,
    id char(64) NOT NULL,
    activity_id char(64) NOT NULL,
    `version` int NOT NULL,
    status tinyint NOT NULL,
    amount int NOT NULL
);
CREATE INDEX activity_status ON tb_red_package (activity_id, status);
CREATE UNIQUE INDEX id ON tb_red_package (id);