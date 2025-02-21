-- 先删除有外键约束的表
DROP TABLE IF EXISTS `test_record`;
DROP TABLE IF EXISTS `sports_item`;
DROP TABLE IF EXISTS `user`;

-- 创建用户表
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `user_type` varchar(20) NOT NULL,
  `real_name` varchar(50),
  `email` varchar(100),
  `phone` varchar(20),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建体育项目表
CREATE TABLE `sports_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `description` text,
  `unit` varchar(20) NOT NULL,
  `type` varchar(20) NOT NULL,
  `is_active` boolean DEFAULT TRUE,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建测试记录表
CREATE TABLE `test_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  `sports_item_id` bigint NOT NULL,
  `score` double NOT NULL,
  `test_time` datetime NOT NULL,
  `remark` text,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`student_id`) REFERENCES `user` (`id`),
  FOREIGN KEY (`teacher_id`) REFERENCES `user` (`id`),
  FOREIGN KEY (`sports_item_id`) REFERENCES `sports_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; 