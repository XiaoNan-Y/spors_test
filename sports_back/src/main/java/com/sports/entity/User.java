package com.sports.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String userType; // ADMIN, STUDENT, TEACHER

    private String realName;
    private String email;
    private String phone;

    public User() {
    }

    public User(Long id) {
        this.id = id;
    }
} 