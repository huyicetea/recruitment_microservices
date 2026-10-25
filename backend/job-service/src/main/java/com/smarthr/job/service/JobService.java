package com.smarthr.job.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.smarthr.job.dto.req.CreateJobReq;
import com.smarthr.job.dto.req.UpdateJobReq;
import com.smarthr.job.dto.res.JobRes;

public interface JobService {

    JobRes createJob(CreateJobReq req);

    JobRes getJobById(UUID id);

    Page<JobRes> getPublishedJobs(Pageable pageable);

    Page<JobRes> getJobsByCompany(UUID companyId, Pageable pageable);

    JobRes updateJob(UUID id, UpdateJobReq req);

    void deleteJob(UUID id);
}