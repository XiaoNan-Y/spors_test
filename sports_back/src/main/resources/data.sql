-- 清空现有数据
DELETE FROM test_record;
DELETE FROM sports_item;
DELETE FROM user;
DELETE FROM notice;

-- 插入管理员用户
INSERT INTO user (username, password, user_type, real_name, email, phone)
VALUES 
('admin', '123456', 'ADMIN', '管理员', 'admin@example.com', '13800000000');

-- 插入测试教师用户
INSERT INTO user (username, password, user_type, real_name, email, phone)
VALUES 
('teacher1', '123456', 'TEACHER', '张老师', 'teacher1@example.com', '13800000001'),
('teacher2', '123456', 'TEACHER', '李老师', 'teacher2@example.com', '13800000002');

-- 插入测试学生用户
INSERT INTO user (username, password, user_type, real_name, email, phone)
VALUES 
('student1', '123456', 'STUDENT', '王同学', 'student1@example.com', '13800000003'),
('student2', '123456', 'STUDENT', '李同学', 'student2@example.com', '13800000004'),
('student3', '123456', 'STUDENT', '张同学', 'student3@example.com', '13800000005'),
('student4', '123456', 'STUDENT', '赵同学', 'student4@example.com', '13800000006'),
('student5', '123456', 'STUDENT', '孙同学', 'student5@example.com', '13800000007');

-- 插入体育项目
INSERT INTO sports_item (name, description, unit, type, is_active)
VALUES 
('100米跑', '100米短跑测试', '秒', '田径', true),
('立定跳远', '原地跳远测试', '米', '田径', true),
('引体向上', '单杠引体向上测试', '个', '力量', true),
('1000米跑', '1000米耐力跑测试', '秒', '田径', true),
('仰卧起坐', '核心力量测试', '个', '力量', true);

-- 插入测试记录
INSERT INTO test_record (student_id, teacher_id, sports_item_id, score, test_time, status, review_comment, review_time, created_at, updated_at)
VALUES 
-- 100米跑测试记录
(4, 2, 1, 13.5, NOW(), 'PENDING', NULL, NULL, NOW(), NOW()),
(5, 2, 1, 14.2, NOW(), 'APPROVED', '成绩正常，通过审核', NOW(), NOW(), NOW()),
(6, 2, 1, 12.8, NOW(), 'REJECTED', '成绩异常，需要重新测试', NOW(), NOW(), NOW()),

-- 立定跳远测试记录
(4, 2, 2, 2.3, NOW(), 'PENDING', NULL, NULL, NOW(), NOW()),
(5, 2, 2, 2.1, NOW(), 'APPROVED', '成绩正常，通过审核', NOW(), NOW(), NOW()),
(6, 2, 2, 2.5, NOW(), 'APPROVED', '成绩正常，表现优秀', NOW(), NOW(), NOW()),

-- 引体向上测试记录
(4, 3, 3, 12, NOW(), 'PENDING', NULL, NULL, NOW(), NOW()),
(5, 3, 3, 15, NOW(), 'APPROVED', '成绩正常，通过审核', NOW(), NOW(), NOW()),
(6, 3, 3, 8, NOW(), 'APPROVED', '成绩正常，继续努力', NOW(), NOW(), NOW()),

-- 1000米跑测试记录
(7, 2, 4, 280.5, NOW(), 'PENDING', NULL, NULL, NOW(), NOW()),
(8, 2, 4, 295.0, NOW(), 'APPROVED', '成绩正常，通过审核', NOW(), NOW(), NOW()),
(7, 2, 4, 350.0, NOW(), 'REJECTED', '成绩不及格，需要加强训练', NOW(), NOW(), NOW()),

-- 仰卧起坐测试记录
(7, 3, 5, 45, NOW(), 'PENDING', NULL, NULL, NOW(), NOW()),
(8, 3, 5, 52, NOW(), 'APPROVED', '成绩优秀，通过审核', NOW(), NOW(), NOW()),
(6, 3, 5, 38, NOW(), 'APPROVED', '成绩达标，通过审核', NOW(), NOW(), NOW());

-- 插入通知公告
INSERT INTO notice (title, content, type, priority, status, create_time, update_time, create_by)
VALUES 
('2024年体育测试开始', '请各位同学准时参加体育测试...', 'TEST_SCHEDULE', 'HIGH', 1, NOW(), NOW(), 1),
('体育馆维修通知', '体育馆将于下周进行维修...', 'SYSTEM_MAINTENANCE', 'NORMAL', 1, NOW(), NOW(), 1);