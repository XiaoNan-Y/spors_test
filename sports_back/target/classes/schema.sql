-- 首先删除具有外键引用的表
DROP TABLE IF EXISTS test_record;
DROP TABLE IF EXISTS notice;

-- 然后删除被引用的表
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS sports_item;

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    user_type VARCHAR(20) NOT NULL,
    real_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20)
);

-- 创建体育项目表
CREATE TABLE IF NOT EXISTS sports_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    unit VARCHAR(20) NOT NULL,
    type VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE
);

-- 创建通知表
CREATE TABLE IF NOT EXISTS notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type VARCHAR(50),
    priority VARCHAR(20),
    status INT,
    create_time DATETIME,
    update_time DATETIME,
    create_by BIGINT,
    FOREIGN KEY (create_by) REFERENCES user(id)
);

-- 创建测试记录表
CREATE TABLE IF NOT EXISTS test_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,           -- 学生ID
    teacher_id BIGINT NOT NULL,           -- 教师ID
    sports_item_id BIGINT NOT NULL,       -- 体测项目ID
    score DOUBLE NOT NULL,                -- 测试成绩
    test_time DATETIME NOT NULL,          -- 测试时间
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- 状态（待审核/已通过/已驳回）
    review_comment TEXT,                  -- 审核意见
    review_time DATETIME,                 -- 审核时间
    created_at DATETIME NOT NULL,         -- 创建时间
    updated_at DATETIME NOT NULL,         -- 更新时间
    FOREIGN KEY (student_id) REFERENCES user(id),
    FOREIGN KEY (teacher_id) REFERENCES user(id),
    FOREIGN KEY (sports_item_id) REFERENCES sports_item(id)
); 