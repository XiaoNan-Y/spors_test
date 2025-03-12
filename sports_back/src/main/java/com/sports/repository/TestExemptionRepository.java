package com.sports.repository;

import com.sports.entity.TestExemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TestExemptionRepository extends JpaRepository<TestExemption, Long>, JpaSpecificationExecutor<TestExemption> {
} 