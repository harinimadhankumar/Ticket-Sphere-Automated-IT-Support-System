package com.powergrid.ticketsystem.repository;

import com.powergrid.ticketsystem.entity.TeamLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamLeadRepository extends JpaRepository<TeamLead, Long> {

    /**
     * Find a team lead by name
     */
    Optional<TeamLead> findByName(String name);

    /**
     * Find all active team leads
     */
    @Query("SELECT tl FROM TeamLead tl WHERE tl.status = 'ACTIVE'")
    List<TeamLead> findAllActive();

    /**
     * Find team leads by status
     */
    List<TeamLead> findAllByStatus(String status);

    /**
     * Check if team lead exists by name
     */
    boolean existsByName(String name);

    /**
     * Find by email
     */
    Optional<TeamLead> findByEmail(String email);
}
