-- 清空现有数据
DELETE FROM test_record;
DELETE FROM sports_item;
DELETE FROM user;
DELETE FROM notice;

-- 插入用户数据
INSERT INTO user (username, password, user_type, real_name, email, phone) VALUES
('admin1', '123456', 'ADMIN', '张管理', 'admin1@example.com', '13800000001'),
('admin2', '123456', 'ADMIN', '李管理', 'admin2@example.com', '13800000002'),
('admin', '123456', 'ADMIN', '管理员', 'admin@example.com', '13800000000');

-- 插入教师数据
INSERT INTO user (username, password, user_type, real_name, email, phone) VALUES
('teacher1', '123456', 'TEACHER', '王老师', 'teacher1@example.com', '13800000003'),
('teacher2', '123456', 'TEACHER', '刘老师', 'teacher2@example.com', '13800000004'),
('teacher3', '123456', 'TEACHER', '陈老师', 'teacher3@example.com', '13800000005'),
('teacher4', '123456', 'TEACHER', '张老师', 'teacher4@example.com', '13800000015'),
('teacher5', '123456', 'TEACHER', '李老师', 'teacher5@example.com', '13800000016'),
('teacher6', '123456', 'TEACHER', '赵老师', 'teacher6@example.com', '13800000017');

-- 插入学生数据
INSERT INTO user (username, password, user_type, real_name, email, phone) VALUES
('student1', '123456', 'STUDENT', '赵同学', 'student1@example.com', '13800000006'),
('student2', '123456', 'STUDENT', '钱同学', 'student2@example.com', '13800000007'),
('student3', '123456', 'STUDENT', '孙同学', 'student3@example.com', '13800000008'),
('student4', '123456', 'STUDENT', '李同学', 'student4@example.com', '13800000009'),
('student5', '123456', 'STUDENT', '周同学', 'student5@example.com', '13800000010'),
('student6', '123456', 'STUDENT', '吴同学', 'student6@example.com', '13800000011'),
('student7', '123456', 'STUDENT', '郑同学', 'student7@example.com', '13800000012'),
('student8', '123456', 'STUDENT', '王同学', 'student8@example.com', '13800000013'),
('student9', '123456', 'STUDENT', '冯同学', 'student9@example.com', '13800000014'),
('student10', '123456', 'STUDENT', '陈同学', 'student10@example.com', '13800000015'),
('student11', '123456', 'STUDENT', '褚同学', 'student11@example.com', '13800000016'),
('student12', '123456', 'STUDENT', '卫同学', 'student12@example.com', '13800000017'),
('student13', '123456', 'STUDENT', '蒋同学', 'student13@example.com', '13800000018'),
('student14', '123456', 'STUDENT', '沈同学', 'student14@example.com', '13800000019'),
('student15', '123456', 'STUDENT', '韩同学', 'student15@example.com', '13800000020');
-- 插入体育项目数据
INSERT INTO sports_item (name, description, unit, type) VALUES
('100米跑', '短跑项目，考察学生速度素质', '秒', '径赛'),
('1000米跑', '中长跑项目，考察学生耐力素质', '秒', '径赛'),
('立定跳远', '原地起跳，考察学生爆发力', '厘米', '田赛'),
('实心球', '投掷项目，考察学生力量素质', '米', '田赛'),
('引体向上', '力量项目，考察学生上肢力量', '个', '体能'),
('仰卧起坐', '核心力量项目，考察学生腹部力量', '个', '体能'),
('50米跑', '短跑项目，考察学生爆发力', '秒', '径赛'),
('跳绳', '耐力项目，考察学生协调性', '个', '体能');

-- 插入测试记录数据
-- 100米跑（正常成绩范围：11-15秒）
INSERT INTO test_record (student_id, teacher_id, sports_item_id, score, test_time, status, created_at, updated_at)
SELECT 
    s.id as student_id,
    t.id as teacher_id,
    si.id as sports_item_id,
    ROUND(11 + RAND() * 4, 2) as score,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) as test_time,
    CASE FLOOR(RAND() * 3) 
        WHEN 0 THEN 'PENDING'
        WHEN 1 THEN 'APPROVED'
        ELSE 'REJECTED'
    END as status,
    NOW() as created_at,
    NOW() as updated_at
FROM user s
CROSS JOIN user t
CROSS JOIN sports_item si
WHERE s.user_type = 'STUDENT' 
AND t.user_type = 'TEACHER'
AND si.name = '100米跑'
LIMIT 10;

-- 1000米跑（正常成绩范围：200-300秒）
INSERT INTO test_record (student_id, teacher_id, sports_item_id, score, test_time, status, created_at, updated_at)
SELECT 
    s.id as student_id,
    t.id as teacher_id,
    si.id as sports_item_id,
    ROUND(200 + RAND() * 100, 2) as score,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) as test_time,
    CASE FLOOR(RAND() * 3) 
        WHEN 0 THEN 'PENDING'
        WHEN 1 THEN 'APPROVED'
        ELSE 'REJECTED'
    END as status,
    NOW() as created_at,
    NOW() as updated_at
FROM user s
CROSS JOIN user t
CROSS JOIN sports_item si
WHERE s.user_type = 'STUDENT' 
AND t.user_type = 'TEACHER'
AND si.name = '1000米跑'
LIMIT 10;

-- 立定跳远（正常成绩范围：180-250厘米）
INSERT INTO test_record (student_id, teacher_id, sports_item_id, score, test_time, status, created_at, updated_at)
SELECT 
    s.id as student_id,
    t.id as teacher_id,
    si.id as sports_item_id,
    ROUND(180 + RAND() * 70, 2) as score,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) as test_time,
    CASE FLOOR(RAND() * 3) 
        WHEN 0 THEN 'PENDING'
        WHEN 1 THEN 'APPROVED'
        ELSE 'REJECTED'
    END as status,
    NOW() as created_at,
    NOW() as updated_at
FROM user s
CROSS JOIN user t
CROSS JOIN sports_item si
WHERE s.user_type = 'STUDENT' 
AND t.user_type = 'TEACHER'
AND si.name = '立定跳远'
LIMIT 10;

-- 添加一些异常成绩的记录（用于测试审核功能）
INSERT INTO test_record (student_id, teacher_id, sports_item_id, score, test_time, status, created_at, updated_at, review_comment)
VALUES
-- 100米异常成绩（太快或太慢）
((SELECT id FROM user WHERE user_type = 'STUDENT' LIMIT 1),
 (SELECT id FROM user WHERE user_type = 'TEACHER' LIMIT 1),
 (SELECT id FROM sports_item WHERE name = '100米跑'),
 8.5, NOW(), 'PENDING', NOW(), NOW(), NULL),

((SELECT id FROM user WHERE user_type = 'STUDENT' LIMIT 1),
 (SELECT id FROM user WHERE user_type = 'TEACHER' LIMIT 1),
 (SELECT id FROM sports_item WHERE name = '100米跑'),
 25.3, NOW(), 'PENDING', NOW(), NOW(), NULL),

-- 1000米异常成绩
((SELECT id FROM user WHERE user_type = 'STUDENT' LIMIT 1),
 (SELECT id FROM user WHERE user_type = 'TEACHER' LIMIT 1),
 (SELECT id FROM sports_item WHERE name = '1000米跑'),
 150.5, NOW(), 'PENDING', NOW(), NOW(), NULL),

-- 立定跳远异常成绩
((SELECT id FROM user WHERE user_type = 'STUDENT' LIMIT 1),
 (SELECT id FROM user WHERE user_type = 'TEACHER' LIMIT 1),
 (SELECT id FROM sports_item WHERE name = '立定跳远'),
 300.0, NOW(), 'PENDING', NOW(), NOW(), NULL);

-- 插入通知测试数据
INSERT INTO `notice` (`