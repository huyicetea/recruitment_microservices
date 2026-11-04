package com.smarthr.job.repo;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smarthr.job.model.entity.Job;
import com.smarthr.job.model.enums.JobStatus;

@Repository
public interface JobRepo extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {

    Page<Job> findByCompanyId(UUID companyId, Pageable pageable);

    List<Job> findByStatusAndDeadlineBefore(JobStatus status, Instant now);

    @Query(value = """
            SELECT * FROM jobs j
            WHERE (:industry IS NULL OR j.industry = :industry)
              AND (:location IS NULL OR j.location = :location)
              AND (:salaryMin IS NULL OR j.salary_max >= :salaryMin)
              AND (:status IS NULL OR j.status = :status)
              AND (:keyword IS NULL OR :keyword = '' OR 
                   to_tsvector('english', j.title || ' ' || j.description) @@ plainto_tsquery('english', :keyword))
            """,
            countQuery = """
            SELECT count(*) FROM jobs j
            WHERE (:industry IS NULL OR j.industry = :industry)
              AND (:location IS NULL OR j.location = :location)
              AND (:salaryMin IS NULL OR j.salary_max >= :salaryMin)
              AND (:status IS NULL OR j.status = :status)
              AND (:keyword IS NULL OR :keyword = '' OR 
                   to_tsvector('english', j.title || ' ' || j.description) @@ plainto_tsquery('english', :keyword))
            """,
            nativeQuery = true)
    Page<Job> searchJobsFts(
            @Param("keyword") String keyword,
            @Param("industry") String industry,
            @Param("location") String location,
            @Param("salaryMin") Long salaryMin,
            @Param("status") String status,
            Pageable pageable);
}