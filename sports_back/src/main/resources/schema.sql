-- 先删除所有表（如果存在）
DROP TABLE IF EXISTS test_record;
DROP TABLE IF EXISTS test_exemption;
DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS sports_item;
DROP TABLE IF EXISTS user;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `username` varchar(50) NOT NULL,
    `password` varchar(100) NOT NULL,
    `user_type` varchar(20) NOT NULL,
    `real_name` varchar(50) DEFAULT NULL,
    `email` varchar(100) DEFAULT NULL,
    `phone` varchar(20) DEFAULT NULL,
    `student_number` varchar(50) DEFAULT NULL,
    `created_at` datetime NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_student_number` (`student_number`),
    KEY `idx_user_type` (`user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 创建体育项目表
CREATE TABLE IF NOT EXISTS `sports_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(50) NOT NULL,
    `description` text,
    `unit` varchar(20) NOT NULL,
    `type` varchar(20) NOT NULL,
    `is_active` boolean DEFAULT true,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 创建测试记录表
CREATE TABLE IF NOT EXISTS `test_record` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `student_number` varchar(50) NOT NULL,
    `sports_item_id` bigint NOT NULL,
    `score` double NOT NULL,
    `class_name` varchar(50) NOT NULL,
    `status` varchar(20) NOT NULL,
    `review_comment` text,
    `review_time` datetime DEFAULT NULL,
    `created_at` datetime NOT NULL,
    `updated_at` datetime NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_student_number` (`student_number`),
    KEY `idx_sports_item_id` (`sports_item_id`),
    KEY `idx_status` (`status`),
    FOREIGN KEY (`student_number`) REFERENCES `user` (`student_number`),
    FOREIGN KEY (`sports_item_id`) REFERENCES `sports_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 创建通知公告表
CREATE TABLE IF NOT EXISTS `notice` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `title` varchar(200) NOT NULL,
    `content` text,
    `type` varchar(50) NOT NULL,
    `priority` varchar(20) NOT NULL,
    `status` int NOT NULL,
    `create_time` datetime NOT NULL,
    `update_time` datetime NOT NULL,
    `create_by` bigint NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 创建免测/重测申请表（如果不存在）
CREATE TABLE IF NOT EXISTS `test_exemption` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `student_number` varchar(50) NOT NULL,
    `class_name` varchar(50),
    `sports_item_id` bigint,
    `type` varchar(20) NOT NULL COMMENT 'EXEMPTION: 免测, RETEST: 重测',
    `reason` text NOT NULL,
    `status` varchar(20) NOT NULL DEFAULT 'PENDING_TEACHER' COMMENT 'PENDING_TEACHER: 待教师审核, PENDING_ADMIN: 待管理员审核, APPROVED: 已通过, REJECTED_TEACHER: 教师驳回, REJECTED: 最终驳回',
    `review_comment` text,
    `reviewer_id` bigint,
    `review_time` datetime,
    `teacher_review_comment` text,
    `admin_review_comment` text,
    `apply_time` datetime NOT NULL,
    `teacher_review_time` datetime,
    `admin_review_time` datetime,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`student_number`) REFERENCES `user`(`student_number`),
    FOREIGN KEY (`sports_item_id`) REFERENCES `sports_item`(`id`),
    FOREIGN KEY (`reviewer_id`) REFERENCES `user`(`id`),
    KEY `idx_student_number` (`student_number`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_class_name` (`class_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 