CREATE TABLE `tb_number_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `status` varchar(16) NOT NULL,
  `min_number` bigint(20) NOT NULL COMMENT '最小虚拟号值',
  `max_number` bigint(20) NOT NULL COMMENT '最大虚拟号值',
  `current_number` bigint(20) NOT NULL COMMENT '当前虚拟号的数字',
  `version` int(11) NOT NULL COMMENT '数据版本',
  `create_time` date NOT NULL COMMENT '创建时间',
  `update_time` date NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;