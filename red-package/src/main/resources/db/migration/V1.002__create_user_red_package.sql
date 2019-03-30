CREATE TABLE tb_user_red_package
(
    `_id` bigint PRIMARY KEY AUTO_INCREMENT,
    user_id char(64) NOT NULL,
    activity_id char(64) NOT NULL,
    red_package_id char(64) NOT NULL,
    amount int NOT NULL
);
CREATE INDEX user_activity ON tb_user_red_package (user_id, activity_id);