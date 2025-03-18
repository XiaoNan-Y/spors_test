package com.sports.repository;

import com.sports.entity.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {
    
    @Query("SELECT COUNT(c) FROM Class c WHERE c.status = 1")
    Integer countActiveClasses();
} 