-- 先删除所有表（注意删除顺序，先删除有外键依赖的表）
DROP TABLE IF EXISTS test_record;
DROP TABLE IF EXISTS test_exemption;
DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS sports_item;
DROP TABLE IF EXISTS user;

-- 创建用户表（最基础的表，其他表都依赖它）
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

-- 创建体育项目表（被 test_record 和 test_exemption 依赖）
CREATE TABLE IF NOT EXISTS `sports_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `description` text,
  `unit` varchar(20) NOT NULL,
  `type` varchar(20) NOT NULL,
  `is_active` boolean DEFAULT true,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 创建通知公告表（独立表）
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

-- 创建免测/重测申请表（依赖 user 和 sports_item）
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

-- 创建测试记录表（依赖 user 和 sports_item）
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

-- 插入基础数据
INSERT INTO user (username, password, user_type, real_name, student_number, email, phone, created_at) 
VALUES 
('admin', '123456', 'ADMIN', '管理员', NULL, 'admin@example.com', '13800000000', NOW()),
('teacher1', '123456', 'TEACHER', '张老师', NULL, 'teacher1@example.com', '13800000001', NOW()),
('student1', '123456', 'STUDENT', '李同学', '2023001', 'student1@example.com', '13800000002', NOW());

-- 插入教师用户
INSERT INTO user (username, password, real_name, user_type, email, phone, created_at)
VALUES 
('teacher2', '123456', '李老师', 'TEACHER', 'teacher2@example.com', '13822222222', NOW()),
('teacher3', '123456', '王老师', 'TEACHER', 'teacher3@example.com', '13833333333', NOW());

-- 插入学生用户
INSERT INTO user (username, password, real_name, user_type, student_number, email, phone, created_at)
VALUES 
('student2', '123456', '李四', 'STUDENT', '2023002', 'student2@example.com', '13855555555', NOW()),
('student3', '123456', '王五', 'STUDENT', '2023003', 'student3@example.com', '13866666666', NOW()),
('student4', '123456', '赵六', 'STUDENT', '2023004', 'student4@example.com', '13877777777', NOW()),
('student5', '123456', '孙七', 'STUDENT', '2023005', 'student5@example.com', '13888888888', NOW());

-- 插入体育项目数据
INSERT INTO sports_item (name, description, unit, type, is_active)
VALUES 
('100米跑', '100米短跑测试', '秒', '田径', true),
('立定跳远', '原地跳远测试', '米', '田径', true),
('引体向上', '单杠引体向上测试', '个', '力量', true),
('仰卧起坐', '腰腹力量测试', '个', '力量', true),
('800/1000米耐力跑', '耐力跑测试', '秒', '田径', true);

-- 插入测试记录
INSERT INTO test_record (
    student_number, 
    sports_item_id, 
    score, 
    class_name, 
    status, 
    review_comment, 
    review_time, 
    created_at, 
    updated_at
) VALUES 
('2023001', 1, 13.5, '计科1班', 'PENDING', NULL, NULL, NOW(), NOW()),
('2023001', 1, 14.2, '计科1班', 'APPROVED', '成绩正常，通过审核', NOW(), NOW(), NOW()),
('2023002', 1, 12.8, '计科2班', 'REJECTED', '成绩异常，需要重新测试', NOW(), NOW(), NOW());

-- 插入通知公告
INSERT INTO notice (title, content, type, priority, status, create_time, update_time, create_by)
VALUES 
('2024年体育测试开始', '请各位同学准时参加体育测试...', 'TEST_SCHEDULE', 'HIGH', 1, NOW(), NOW(), 1),
('体育馆维修通知', '体育馆将于下周进行维修...', 'SYSTEM_MAINTENANCE', 'NORMAL', 1, NOW(), NOW(), 1);

-- 注意：不要在这里初始化 test_exemption 表的数据