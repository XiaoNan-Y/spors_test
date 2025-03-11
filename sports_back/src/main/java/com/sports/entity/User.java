package com.sports.entity;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "user", indexes = {
    @Index(name = "idx_student_number", columnList = "student_number"),
    @Index(name = "idx_user_type", columnList = "user_type")
})
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "user_type", nullable = false, length = 20)
    private String userType; // ADMIN, STUDENT, TEACHER

    @Column(name = "real_name", length = 50)
    private String realName;
    
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "student_number", length = 50)
    private String studentNumber;

    public User() {
    }

    public User(Long id) {
        this.id = id;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    // 用户类型常量
    public static final String TYPE_ADMIN = "ADMIN";
    public static final String TYPE_STUDENT = "STUDENT";
    public static final String TYPE_TEACHER = "TEACHER";

    // 判断用户类型的便捷方法
    public boolean isAdmin() {
        return TYPE_ADMIN.equals(this.userType);
    }

    public boolean isStudent() {
        return TYPE_STUDENT.equals(this.userType);
    }

    public boolean isTeacher() {
        return TYPE_TEACHER.equals(this.userType);
    }
} 