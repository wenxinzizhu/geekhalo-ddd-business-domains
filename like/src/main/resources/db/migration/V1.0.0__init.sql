CREATE TABLE `tb_like` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `version` int(11) NOT NULL,
  `owner_id` bigint(20) NOT NULL,
  `owner_type` int(11) NOT NULL,
  `status` int(11) DEFAULT NULL,
  `target_id` bigint(20) NOT NULL,
  `target_type` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_owner_target` (`owner_type`,`owner_id`,`target_type`,`target_id`)
);

CREATE TABLE `tb_like_logger` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `version` int(11) NOT NULL,
  `action_type` int(11) DEFAULT NULL,
  `owner_id` bigint(20) NOT NULL,
  `owner_type` int(11) NOT NULL,
  `target_id` bigint(20) NOT NULL,
  `target_type` int(11) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `tb_target_count` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `version` int(11) NOT NULL,
  `count` bigint(20) DEFAULT NULL,
  `target_id` bigint(20) NOT NULL,
  `target_type` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_target` (`target_type`,`target_id`)
);