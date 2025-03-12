-- 先删除所有表（如果存在）
DROP TABLE IF EXISTS test_record;
DROP TABLE IF EXISTS sports_item;
DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS test_exemption;

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
  `test_time` datetime NOT NULL,
  `status` varchar(20) NOT NULL,
  `review_comment` text,
  `review_time` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_student_number` (`student_number`),
  KEY `idx_sports_item_id` (`sports_item_id`),
  KEY `idx_status` (`status`)
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

-- 创建免测/重测申请表
CREATE TABLE IF NOT EXISTS `test_exemption` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_number` varchar(50) NOT NULL,
  `reason` text NOT NULL,
  `type` varchar(50) NOT NULL,
  `status` varchar(50) NOT NULL,
  `apply_time` datetime NOT NULL,
  `teacher_review_time` datetime DEFAULT NULL,
  `teacher_review_comment` text,
  `admin_review_time` datetime DEFAULT NULL,
  `admin_review_comment` text,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_student_number` (`student_number`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 清除现有数据
DELETE FROM user;

-- 插入管理员用户
INSERT INTO user (username, password, real_name, user_type, email, phone, created_at)
VALUES ('admin', '123456', '系统管理员', 'ADMIN', 'admin@example.com', '13800000000', NOW());

-- 插入教师用户
INSERT INTO user (username, password, real_name, user_type, email, phone, created_at)
VALUES 
('teacher1', '123456', '张老师', 'TEACHER', 'teacher1@example.com', '13811111111', NOW()),
('teacher2', '123456', '李老师', 'TEACHER', 'teacher2@example.com', '13822222222', NOW()),
('teacher3', '123456', '王老师', 'TEACHER', 'teacher3@example.com', '13833333333', NOW());

-- 插入学生用户
INSERT INTO user (username, password, real_name, user_type, student_number, email, phone, created_at)
VALUES 
('student1', '123456', '张三', 'STUDENT', '2023001', 'student1@example.com', '13844444444', NOW()),
('student2', '123456', '李四', 'STUDENT', '2023002', 'student2@example.com', '13855555555', NOW()),
('student3', '123456', '王五', 'STUDENT', '2023003', 'student3@example.com', '13866666666', NOW()),
('student4', '123456', '赵六', 'STUDENT', '2023004', 'student4@example.com', '13877777777', NOW()),
('student5', '123456', '孙七', 'STUDENT', '2023005', 'student5@example.com', '13888888888', NOW());

-- 插入体育项目数据
INSERT INTO sports_item (name, description, unit, type, is_active)
SELECT '100米跑', '100米短跑测试', '秒', '田径', true
WHERE NOT EXISTS (SELECT 1 FROM sports_item WHERE name = '100米跑');

INSERT INTO sports_item (name, description, unit, type, is_active)
SELECT '立定跳远', '原地跳远测试', '米', '田径', true
WHERE NOT EXISTS (SELECT 1 FROM sports_item WHERE name = '立定跳远');

INSERT INTO sports_item (name, description, unit, type, is_active)
SELECT '引体向上', '单杠引体向上测试', '个', '力量', true
WHERE NOT EXISTS (SELECT 1 FROM sports_item WHERE name = '引体向上');

INSERT INTO sports_item (name, description, unit, type, is_active)
SELECT '仰卧起坐', '腰腹力量测试', '个', '力量', true
WHERE NOT EXISTS (SELECT 1 FROM sports_item WHERE name = '仰卧起坐');

INSERT INTO sports_item (name, description, unit, type, is_active)
SELECT '800/1000米耐力跑', '耐力跑测试', '秒', '田径', true
WHERE NOT EXISTS (SELECT 1 FROM sports_item WHERE name = '800/1000米耐力跑');

-- 插入测试记录
INSERT INTO test_record (
    student_number, 
    sports_item_id, 
    score, 
    test_time, 
    status, 
    review_comment, 
    review_time, 
    created_at, 
    updated_at
) VALUES 
-- 100米跑测试记录
('2023001', 1, 13.5, NOW(), 'PENDING', NULL, NULL, NOW(), NOW()),
('2023001', 1, 14.2, NOW(), 'APPROVED', '成绩正常，通过审核', NOW(), NOW(), NOW()),
('2023002', 1, 12.8, NOW(), 'REJECTED', '成绩异常，需要重新测试', NOW(), NOW(), NOW()),

-- 立定跳远测试记录
('2023001', 2, 2.3, NOW(), 'PENDING', NULL, NULL, NOW(), NOW()),
('2023002', 2, 2.1, NOW(), 'APPROVED', '成绩正常，通过审核', NOW(), NOW(), NOW()),
('2023003', 2, 2.5, NOW(), 'APPROVED', '成绩正常，表现优秀', NOW(), NOW(), NOW()),

-- 引体向上测试记录
('2023001', 3, 12, NOW(), 'PENDING', NULL, NULL, NOW(), NOW()),
('2023002', 3, 15, NOW(), 'APPROVED', '成绩正常，通过审核', NOW(), NOW(), NOW()),
('2023003', 3, 8, NOW(), 'APPROVED', '成绩正常，继续努力', NOW(), NOW(), NOW());

-- 插入通知公告
INSERT INTO notice (title, content, type, priority, status, create_time, update_time, create_by)
VALUES 
('2024年体育测试开始', '请各位同学准时参加体育测试...', 'TEST_SCHEDULE', 'HIGH', 1, NOW(), NOW(), 1),
('体育馆维修通知', '体育馆将于下周进行维修...', 'SYSTEM_MAINTENANCE', 'NORMAL', 1, NOW(), NOW(), 1);

-- 插入免测/重测申请测试数据
INSERT INTO test_exemption (
    student_number,
    reason,
    type,
    status,
    apply_time,
    teacher_review_time,
    teacher_review_comment,
    admin_review_time,
    admin_review_comment,
    created_at,
    updated_at
) VALUES 
-- 待教师审核的申请
('2023001', '因腿部受伤需要休养两个月', 'EXEMPTION', 'PENDING_TEACHER', 
 NOW(), NULL, NULL, NULL, NULL, NOW(), NOW()),

('2023002', '上次测试时身体不适，申请重新测试', 'RETEST', 'PENDING_TEACHER',
 NOW(), NULL, NULL, NULL, NULL, NOW(), NOW()),

-- 教师已审核，待管理员审核的申请
('2023003', '最近做了手术需要恢复', 'EXEMPTION', 'PENDING_ADMIN',
 DATE_SUB(NOW(), INTERVAL 2 DAY), 
 DATE_SUB(NOW(), INTERVAL 1 DAY),
 '已查看医院证明，情况属实，建议批准', 
 NULL, NULL, 
 DATE_SUB(NOW(), INTERVAL 2 DAY), 
 DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 教师驳回的申请
('2023004', '最近感冒不适', 'EXEMPTION', 'REJECTED_TEACHER',
 DATE_SUB(NOW(), INTERVAL 3 DAY),
 DATE_SUB(NOW(), INTERVAL 2 DAY),
 '普通感冒不建议免测，建议调整好状态参加测试',
 NULL, NULL,
 DATE_SUB(NOW(), INTERVAL 3 DAY),
 DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- 管理员通过的申请
('2023005', '因参加省级比赛训练，申请延期测试', 'RETEST', 'APPROVED',
 DATE_SUB(NOW(), INTERVAL 5 DAY),
 DATE_SUB(NOW(), INTERVAL 4 DAY),
 '已确认比赛通知，建议批准',
 DATE_SUB(NOW(), INTERVAL 3 DAY),
 '同意教师意见，准予延期测试',
 DATE_SUB(NOW(), INTERVAL 5 DAY),
 DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- 管理员驳回的申请
('2023001', '课程时间冲突', 'RETEST', 'REJECTED',
 DATE_SUB(NOW(), INTERVAL 6 DAY),
 DATE_SUB(NOW(), INTERVAL 5 DAY),
 '时间冲突可以调整，不建议批准',
 DATE_SUB(NOW(), INTERVAL 4 DAY),
 '同意教师意见，请调整时间参加测试',
 DATE_SUB(NOW(), INTERVAL 6 DAY),
 DATE_SUB(NOW(), INTERVAL 4 DAY));