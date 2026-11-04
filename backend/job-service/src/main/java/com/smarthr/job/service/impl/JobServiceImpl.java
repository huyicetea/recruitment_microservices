package com.smarthr.job.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthr.job.dto.req.JobReq;
import com.smarthr.job.dto.res.JobRes;
import com.smarthr.job.mapper.JobMapper;
import com.smarthr.job.model.entity.Job;
import com.smarthr.job.model.entity.JobSkill;
import com.smarthr.job.model.entity.Outbox;
import com.smarthr.job.model.enums.JobStatus;
import com.smarthr.job.repo.JobRepo;
import com.smarthr.job.repo.OutboxRepo;
import com.smarthr.job.service.JobService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepo jobRepo;
    private final OutboxRepo outboxRepo;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public JobRes createJob(JobReq req, UUID companyId, UUID userId) {
        Job job = JobMapper.toEntity(req);
        job.setCompanyId(companyId);
        job.setCreatedBy(userId);
        job.setStatus(JobStatus.OPEN);

        Job savedJob = jobRepo.save(job);

        saveOutboxEvent("job.created", savedJob);

        return JobMapper.toRes(savedJob);
    }

    @Override
    @Transactional
    public JobRes updateJob(UUID id, JobReq req, UUID companyId) {
        Job job = jobRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + id));

        if (!job.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Unauthorized to update job of another company");
        }

        job.setTitle(req.getTitle());
        job.setDescription(req.getDescription());
        job.setLevel(req.getLevel());
        job.setJobType(req.getJobType());
        job.setWorkMode(req.getWorkMode());
        job.setLocation(req.getLocation());
        job.setIndustry(req.getIndustry());
        job.setSalaryMin(req.getSalaryMin());
        job.setSalaryMax(req.getSalaryMax());
        job.setMinYearsExp(req.getMinYearsExp() != null ? req.getMinYearsExp() : 0);
        job.setDeadline(req.getDeadline());

        job.getSkills().clear();
        if (req.getSkills() != null) {
            List<JobSkill> newSkills = req.getSkills().stream()
                    .map(s -> JobMapper.toSkillEntity(s, job))
                    .collect(Collectors.toList());
            job.getSkills().addAll(newSkills);
        }

        Job updatedJob = jobRepo.save(job);

        saveOutboxEvent("job.updated", updatedJob);

        return JobMapper.toRes(updatedJob);
    }

    @Override
    @Transactional
    public JobRes closeJob(UUID id, UUID companyId) {
        Job job = jobRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + id));

        if (!job.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Unauthorized to close job of another company");
        }

        job.setStatus(JobStatus.CLOSED);
        Job closedJob = jobRepo.save(job);

        saveOutboxEvent("job.closed", closedJob);

        return JobMapper.toRes(closedJob);
    }

    @Override
    @Transactional(readOnly = true)
    public JobRes getJobById(UUID id) {
        Job job = jobRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + id));
        return JobMapper.toRes(job);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobRes> getJobsByCompany(UUID companyId, Pageable pageable) {
        return jobRepo.findByCompanyId(companyId, pageable)
                .map(JobMapper::toRes);
    }

@   Override
    @Transactional(readOnly = true)
    public Page<JobRes> searchJobs(String keyword, String industry, String location, Long salaryMin, JobStatus status, Pageable pageable) {
        String statusStr = status != null ? status.name() : null;
        return jobRepo.searchJobsFts(keyword, industry, location, salaryMin, statusStr, pageable)
                .map(JobMapper::toRes);
    }

    private void saveOutboxEvent(String eventType, Job job) {
        try {
            JobRes payloadRes = JobMapper.toRes(job);
            String payloadJson = objectMapper.writeValueAsString(payloadRes);

            Outbox outbox = Outbox.builder()
                    .aggregateType("JOB")
                    .aggregateId(job.getId().toString())
                    .type(eventType)
                    .payload(payloadJson)
                    .build();

            outboxRepo.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing job outbox event payload", e);
        }
    }
}