-- 首先删除具有外键引用的表
DROP TABLE IF EXISTS test_record;
DROP TABLE IF EXISTS test_exemption;
DROP TABLE IF EXISTS notice;

-- 然后删除被引用的表
DROP TABLE IF EXISTS sports_item;
DROP TABLE IF EXISTS user;

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    user_type VARCHAR(20) NOT NULL,
    real_name VARCHAR(50),
    student_number VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    created_at DATETIME NOT NULL
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
    student_number VARCHAR(50) NOT NULL,
    sports_item_id BIGINT NOT NULL,
    score DOUBLE NOT NULL,
    test_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    review_comment TEXT,
    review_time DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (student_number) REFERENCES user(student_number),
    FOREIGN KEY (sports_item_id) REFERENCES sports_item(id)
);

-- 修改免测申请表
DROP TABLE IF EXISTS test_exemption;
CREATE TABLE IF NOT EXISTS test_exemption (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_number VARCHAR(50) NOT NULL,
    reason TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,  -- EXEMPTION(免测) 或 RETEST(重测)
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_TEACHER',  -- 审核状态
    apply_time DATETIME NOT NULL,
    teacher_review_time DATETIME,
    teacher_review_comment TEXT,
    admin_review_time DATETIME,
    admin_review_comment TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (student_number) REFERENCES user(student_number)
); 