package com.sports.repository;

import com.sports.entity.TestExemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TestExemptionRepository extends JpaRepository<TestExemption, Long>, JpaSpecificationExecutor<TestExemption> {
    @Query("SELECT e FROM TestExemption e " +
           "LEFT JOIN e.student s " +
           "LEFT JOIN e.sportsItem si " +
           "WHERE (:className IS NULL OR e.className = :className) " +
           "AND (:type IS NULL OR e.type = :type) " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:keyword IS NULL OR " +
           "LOWER(s.realName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.studentNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<TestExemption> findByFilters(
        @Param("className") String className,
        @Param("type") String type,
        @Param("status") String status,
        @Param("keyword") String keyword,
        Pageable pageable
    );
} 