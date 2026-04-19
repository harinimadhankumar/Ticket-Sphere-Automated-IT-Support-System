package com.powergrid.ticketsystem.repository;

import com.powergrid.ticketsystem.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {

    /**
     * Find department by code
     */
    Optional<DepartmentEntity> findByCode(String code);

    /**
     * Check if department exists by code
     */
    boolean existsByCode(String code);

    /**
     * Get all departments sorted by code
     */
    List<DepartmentEntity> findAllByOrderByCodeAsc();
}
