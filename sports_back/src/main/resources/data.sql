-- 先删除有外键约束的表的数据
DELETE FROM test_record;
DELETE FROM sports_item;
DELETE FROM user;

-- 插入管理员用户
INSERT INTO user (username, password, user_type, real_name, email, phone) VALUES
('admin1', '123456', 'ADMIN', '张管理', 'admin1@example.com', '13800000001'),
('admin2', '123456', 'ADMIN', '李管理', 'admin2@example.com', '13800000002'),
('admin', '123456', 'ADMIN', '管理员', 'admin@example.com', '13800000000');

-- 插入教师用户
INSERT INTO user (username, password, user_type, real_name, email, phone) VALUES
('teacher1', '123456', 'TEACHER', '王老师', 'teacher1@example.com', '13800000003'),
('teacher2', '123456', 'TEACHER', '刘老师', 'teacher2@example.com', '13800000004'),
('teacher3', '123456', 'TEACHER', '陈老师', 'teacher3@example.com', '13800000005'),
('teacher4', '123456', 'TEACHER', '张老师', 'teacher4@example.com', '13800000015'),
('teacher5', '123456', 'TEACHER', '李老师', 'teacher5@example.com', '13800000016'),
('teacher6', '123456', 'TEACHER', '赵老师', 'teacher6@example.com', '13800000017');

-- 插入更多学生用户
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

-- 插入更多测试记录
INSERT INTO test_record (student_id, teacher_id, sports_item_id, score, test_time, remark)
SELECT 
    u1.id as student_id,
    u2.id as teacher_id,
    si.id as sports_item_id,
    CASE 
        WHEN si.unit = '秒' THEN ROUND(RAND() * 20 + 10, 2)
        WHEN si.unit = '厘米' THEN ROUND(RAND() * 100 + 150, 2)
        WHEN si.unit = '米' THEN ROUND(RAND() * 10 + 5, 2)
        WHEN si.unit = '个' THEN FLOOR(RAND() * 30 + 10)
    END as score,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) as test_time,
    CASE 
        WHEN RAND() > 0.8 THEN '优秀表现'
        WHEN RAND() > 0.6 THEN '正常发挥'
        WHEN RAND() > 0.4 THEN '状态一般'
        ELSE '需要加强'
    END as remark
FROM user u1
CROSS JOIN user u2
CROSS JOIN sports_item si
WHERE u1.user_type = 'STUDENT'
AND u2.user_type = 'TEACHER'
LIMIT 200; 
-- 插入通知测试数据
INSERT INTO `notice` (`title`, `content`, `type`, `priority`, `status`, `create_time`, `create_by`) VALUES 
('2024年春季体测安排通知', '各位同学：\n\n2024年春季体测将于3月15日至3月30日进行，请各班按照以下安排参加测试：\n\n1. 3月15日-3月20日：大一年级\n2. 3月21日-3月25日：大二年级\n3. 3月26日-3月30日：大三年级\n\n请提前做好准备，穿着运动服装参加测试。', 'TEST_SCHEDULE', 'HIGH', 1, '2024-03-01 10:00:00', 1);

INSERT INTO `notice` (`title`, `content`, `type`, `priority`, `status`, `create_time`, `create_by`) VALUES 
('2023年秋季体测成绩公布', '2023年秋季体测成绩已经公布，请登录系统查看个人成绩。如有疑问，请在一周内联系体育教师。', 'SCORE_RELEASE', 'NORMAL', 1, '2024-01-15 14:30:00', 1);

INSERT INTO `notice` (`title`, `content`, `type`, `priority`, `status`, `create_time`, `create_by`) VALUES 
('系统维护通知', '体育测试管理系统将于2024年3月10日晚上22:00-24:00进行系统维护，维护期间系统将暂停使用。给您带来的不便敬请谅解。', 'SYSTEM_MAINTENANCE', 'HIGH', 1, '2024-03-08 09:00:00', 1);

INSERT INTO `notice` (`title`, `content`, `type`, `priority`, `status`, `create_time`, `create_by`) VALUES 
('体育教师培训通知', '定于2024年3月5日下午14:00在体育馆会议室进行新版体测系统使用培训，请所有体育教师准时参加。', 'OTHER', 'NORMAL', 1, '2024-03-02 11:00:00', 1);

INSERT INTO `notice` (`title`, `content`, `type`, `priority`, `status`, `create_time`, `create_by`) VALUES 
('关于补测的通知', '因故未能参加正常体测的同学，可在4月5日至4月10日期间进行补测。请需要补测的同学提前向体育教师报名。', 'TEST_SCHEDULE', 'NORMAL', 1, '2024-03-25 16:00:00', 1);