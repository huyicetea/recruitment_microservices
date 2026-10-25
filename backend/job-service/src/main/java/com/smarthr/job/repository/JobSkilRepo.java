package com.smarthr.job.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smarthr.job.model.entity.JobSkill;

@Repository 
public interface JobSkilRepo extends JpaRepository<JobSkill, JobSkill.JobSkillId> {

    List<JobSkill> findByJobId(UUID jobId);

    void deleteByJobId(UUID jobId);
}