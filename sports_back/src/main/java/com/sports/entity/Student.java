package com.sports.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 20)
    private String realName;
    
    @Column(length = 20)
    private String studentNumber;
    
    @Column(length = 20)
    private String className;
    
    // 其他字段...
} 