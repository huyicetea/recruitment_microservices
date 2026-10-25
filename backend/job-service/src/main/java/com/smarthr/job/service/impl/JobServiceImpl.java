package com.smarthr.job.service.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smarthr.job.dto.req.CreateJobReq;
import com.smarthr.job.dto.req.JobSkillReq;
import com.smarthr.job.dto.req.UpdateJobReq;
import com.smarthr.job.dto.res.JobRes;
import com.smarthr.job.dto.res.JobSkillRes;
import com.smarthr.job.exception.ResourceNotFoundException;
import com.smarthr.job.model.entity.Job;
import com.smarthr.job.model.entity.JobSkill;
import com.smarthr.job.model.entity.Skill;
import com.smarthr.job.model.enums.JobStatus;
import com.smarthr.job.repository.JobRepo;
import com.smarthr.job.repository.SkillRepo;
import com.smarthr.job.service.JobService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepo jobRepo;
    private final SkillRepo skillRepo;

    @Override
    @Transactional
    public JobRes createJob(CreateJobReq req) {
        Job job = Job.builder()
                .companyId(req.getCompanyId())
                .recruiterId(req.getRecruiterId())
                .title(req.getTitle())
                .description(req.getDescription())
                .requirements(req.getRequirements())
                .benefits(req.getBenefits())
                .salaryMin(req.getSalaryMin())
                .salaryMax(req.getSalaryMax())
                .currency(req.getCurrency())
                .location(req.getLocation())
                .employmentType(req.getEmploymentType())
                .experienceLevel(req.getExperienceLevel())
                .status(JobStatus.DRAFT)
                .build();

        if (req.getSkills() != null && !req.getSkills().isEmpty()) {
            attachSkillsToJob(job, req.getSkills());
        }

        Job savedJob = jobRepo.save(job);
        return mapToJobRes(savedJob);
    }

    @Override
    @Transactional(readOnly = true)
    public JobRes getJobById(UUID id) {
        Job job = jobRepo.findByIdWithSkills(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        return mapToJobRes(job);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobRes> getPublishedJobs(Pageable pageable) {
        return jobRepo.findByStatus(JobStatus.PUBLISHED, pageable)
                .map(this::mapToJobRes);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobRes> getJobsByCompany(UUID companyId, Pageable pageable) {
        return jobRepo.findByCompanyId(companyId, pageable)
                .map(this::mapToJobRes);
    }

    @Override
    @Transactional
    public JobRes updateJob(UUID id, UpdateJobReq req) {
        Job job = jobRepo.findByIdWithSkills(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));

        if (req.getTitle() != null) job.setTitle(req.getTitle());
        if (req.getDescription() != null) job.setDescription(req.getDescription());
        if (req.getRequirements() != null) job.setRequirements(req.getRequirements());
        if (req.getBenefits() != null) job.setBenefits(req.getBenefits());
        if (req.getSalaryMin() != null) job.setSalaryMin(req.getSalaryMin());
        if (req.getSalaryMax() != null) job.setSalaryMax(req.getSalaryMax());
        if (req.getCurrency() != null) job.setCurrency(req.getCurrency());
        if (req.getLocation() != null) job.setLocation(req.getLocation());
        if (req.getEmploymentType() != null) job.setEmploymentType(req.getEmploymentType());
        if (req.getExperienceLevel() != null) job.setExperienceLevel(req.getExperienceLevel());
        if (req.getStatus() != null) job.setStatus(req.getStatus());

        if (req.getSkills() != null) {
            job.getJobSkills().clear();
            attachSkillsToJob(job, req.getSkills());
        }

        Job updatedJob = jobRepo.save(job);
        return mapToJobRes(updatedJob);
    }

    @Override
    @Transactional
    public void deleteJob(UUID id) {
        if (!jobRepo.existsById(id)) {
            throw new ResourceNotFoundException("Job not found with id: " + id);
        }
        jobRepo.deleteById(id);
    }

    private void attachSkillsToJob(Job job, Set<JobSkillReq> skillRequests) {
        for (JobSkillReq req : skillRequests) {
            Skill skill = skillRepo.findByNameIgnoreCase(req.getSkillName().trim())
                    .orElseGet(() -> skillRepo.save(
                            Skill.builder()
                                    .name(req.getSkillName().trim())
                                    .build()
                    ));

            JobSkill.JobSkillId compositeId = new JobSkill.JobSkillId(job.getId(), skill.getId());

            JobSkill jobSkill = JobSkill.builder()
                    .id(compositeId)
                    .job(job)
                    .skill(skill)
                    .requiredLevel(req.getRequiredLevel() != null ? req.getRequiredLevel() : "REQUIRED")
                    .build();

            job.getJobSkills().add(jobSkill);
        }
    }

    private JobRes mapToJobRes(Job job) {
        Set<JobSkillRes> skillResponses = new HashSet<>();
        if (job.getJobSkills() != null) {
            skillResponses = job.getJobSkills().stream()
                    .map(js -> JobSkillRes.builder()
                            .skillId(js.getSkill().getId())
                            .skillName(js.getSkill().getName())
                            .category(js.getSkill().getCategory())
                            .requiredLevel(js.getRequiredLevel())
                            .build())
                    .collect(Collectors.toSet());
        }

        return JobRes.builder()
                .id(job.getId())
                .companyId(job.getCompanyId())
                .recruiterId(job.getRecruiterId())
                .title(job.getTitle())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .benefits(job.getBenefits())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .currency(job.getCurrency())
                .location(job.getLocation())
                .employmentType(job.getEmploymentType())
                .experienceLevel(job.getExperienceLevel())
                .status(job.getStatus())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .skills(skillResponses)
                .build();
    }
}