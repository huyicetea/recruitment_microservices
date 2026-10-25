package com.smarthr.job.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smarthr.job.model.entity.Job;
import com.smarthr.job.model.enums.JobStatus;

@Repository 
public interface JobRepo extends JpaRepository<Job, UUID> {
    
    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    Page<Job> findByCompanyId(UUID companyId, Pageable pageable);

    Page<Job> findByRecruiterId(UUID recruiterId, Pageable pageable);

    @Query(
        "select j from Job j " +
        "left join fetch j.jobSkills js " +
        "left join fetch js.skill " +
        "where j.id = :id"
    )
    Optional<Job> findByIdWithSkills(@Param("id") UUID id);

}