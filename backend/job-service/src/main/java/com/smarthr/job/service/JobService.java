package com.smarthr.job.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.smarthr.job.dto.req.JobReq;
import com.smarthr.job.dto.res.JobRes;
import com.smarthr.job.model.enums.JobStatus;

public interface JobService {
    JobRes createJob(JobReq req, UUID companyId, UUID userId);
    JobRes updateJob(UUID id, JobReq req, UUID companyId);
    JobRes closeJob(UUID id, UUID companyId);
    JobRes getJobById(UUID id);
    Page<JobRes> getJobsByCompany(UUID companyId, Pageable pageable);
    Page<JobRes> searchJobs(String industry, String location, Long salaryMin, JobStatus status, Pageable pageable);
}