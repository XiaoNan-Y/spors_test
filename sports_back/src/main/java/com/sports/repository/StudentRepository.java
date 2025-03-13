package com.sports.repository;

import com.sports.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    
    /**
     * 根据学号查找学生
     */
    Optional<Student> findByStudentNumber(String studentNumber);
    
    /**
     * 根据班级查找学生
     */
    List<Student> findByClassName(String className);
    
    /**
     * 根据姓名或学号模糊查询
     */
    List<Student> findByRealNameContainingOrStudentNumberContaining(String realName, String studentNumber);
} 