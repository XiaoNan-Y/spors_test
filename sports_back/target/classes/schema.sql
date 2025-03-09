-- 首先删除具有外键引用的表
DROP TABLE IF EXISTS test_record;
DROP TABLE IF EXISTS notice;

-- 然后删除被引用的表
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS sports_item;

-- 创建用户表
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    user_type VARCHAR(20) NOT NULL,
    real_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20)
);

-- 创建体育项目表
CREATE TABLE sports_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    unit VARCHAR(20),
    type VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE
);

-- 创建通知表
CREATE TABLE notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type VARCHAR(50),
    priority VARCHAR(20),
    status INTEGER DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    FOREIGN KEY (create_by) REFERENCES user(id)
);

-- 创建测试记录表
CREATE TABLE test_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT,
    teacher_id BIGINT,
    sports_item_id BIGINT,
    score DOUBLE,
    test_time DATETIME,
    status VARCHAR(20) DEFAULT 'PENDING',
    remark TEXT,
    review_comment TEXT,
    review_time DATETIME,
    reviewer_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (student_id) REFERENCES user(id),
    FOREIGN KEY (teacher_id) REFERENCES user(id),
    FOREIGN KEY (reviewer_id) REFERENCES user(id),
    FOREIGN KEY (sports_item_id) REFERENCES sports_item(id)
); 