package com.smarthr.job.service.impl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smarthr.common.event.JobEvent;
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
import com.smarthr.job.service.JobEventPublisher;
import com.smarthr.job.service.JobService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepo jobRepo;
    private final SkillRepo skillRepo;
    private final JobEventPublisher jobEventPublisher;

    @Override
    @Transactional
    public JobRes createJob(CreateJobReq req) {
        validateSalary(req.getSalaryMin(), req.getSalaryMax());

        Job job = Job.builder()
                .companyId(req.getCompanyId())
                .recruiterId(req.getRecruiterId())
                .title(req.getTitle())
                .description(req.getDescription())
                .requirements(req.getRequirements())
                .benefits(req.getBenefits())
                .salaryMin(req.getSalaryMin())
                .salaryMax(req.getSalaryMax())
                .currency(req.getCurrency() != null && !req.getCurrency().isBlank() ? req.getCurrency() : "VND")
                .location(req.getLocation())
                .industry(req.getIndustry())
                .workMode(req.getWorkMode())
                .minYearsExp(req.getMinYearsExp())
                .deadline(req.getDeadline())
                .employmentType(req.getEmploymentType())
                .experienceLevel(req.getExperienceLevel())
                .status(JobStatus.DRAFT)
                .build();

        Job savedJob = jobRepo.save(job);

        if (req.getSkills() != null && !req.getSkills().isEmpty()) {
            attachSkillsToJob(savedJob, req.getSkills());
            savedJob = jobRepo.save(savedJob);
        }

        jobEventPublisher.publish("job.created", toJobEvent("job.created", savedJob));

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
        if (req.getIndustry() != null) job.setIndustry(req.getIndustry());
        if (req.getWorkMode() != null) job.setWorkMode(req.getWorkMode());
        if (req.getMinYearsExp() != null) job.setMinYearsExp(req.getMinYearsExp());
        if (req.getDeadline() != null) job.setDeadline(req.getDeadline());
        if (req.getEmploymentType() != null) job.setEmploymentType(req.getEmploymentType());
        if (req.getExperienceLevel() != null) job.setExperienceLevel(req.getExperienceLevel());
        if (req.getStatus() != null) job.setStatus(req.getStatus());

        validateSalary(job.getSalaryMin(), job.getSalaryMax());

        if (req.getSkills() != null) {
            job.getJobSkills().clear();
            jobRepo.saveAndFlush(job);

            attachSkillsToJob(job, req.getSkills());
        }

        Job updatedJob = jobRepo.save(job);
        
        jobEventPublisher.publish("job.updated", toJobEvent("job.updated", updatedJob));

        return mapToJobRes(updatedJob);
    }

    @Override
    @Transactional
    public void deleteJob(UUID id) {
        Job job = jobRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        
        jobRepo.deleteById(id);

        JobEvent deleteEvent = JobEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("job.deleted")
                .jobId(id)
                .companyId(job.getCompanyId())
                .timestamp(OffsetDateTime.now())
                .build();
        jobEventPublisher.publish("job.deleted", deleteEvent);
    }

    private void attachSkillsToJob(Job job, Set<JobSkillReq> skillRequests) {
        Set<Integer> addedSkillIds = new HashSet<>();

        for (JobSkillReq req : skillRequests) {
            Skill skill;

            if (req.getSkillId() != null) {
                skill = skillRepo.findById(req.getSkillId())
                        .orElseGet(() -> findOrCreateSkillByName(req.getSkillName()));
            } else {
                skill = findOrCreateSkillByName(req.getSkillName());
            }

            if (!addedSkillIds.add(skill.getId())) {
                continue;
            }

            JobSkill.JobSkillId compositeId = new JobSkill.JobSkillId(job.getId(), skill.getId());

            JobSkill jobSkill = JobSkill.builder()
                    .id(compositeId)
                    .job(job)
                    .skill(skill)
                    .requiredLevel(req.getRequiredLevel() != null && !req.getRequiredLevel().isBlank()
                            ? req.getRequiredLevel()
                            : "REQUIRED")
                    .mustHave(req.getMustHave() != null ? req.getMustHave() : false)
                    .build();

            job.getJobSkills().add(jobSkill);
        }
    }

    private Skill findOrCreateSkillByName(String rawName) {
        String cleanName = rawName.trim();
        return skillRepo.findByNameIgnoreCase(cleanName)
                .orElseGet(() -> skillRepo.save(
                        Skill.builder()
                                .name(cleanName)
                                .build()
                ));
    }

    private void validateSalary(BigDecimal min, BigDecimal max) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Minimum salary cannot be greater than maximum salary");
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
                            .mustHave(js.getMustHave())
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
                .industry(job.getIndustry())
                .workMode(job.getWorkMode())
                .minYearsExp(job.getMinYearsExp())
                .deadline(job.getDeadline())
                .employmentType(job.getEmploymentType())
                .experienceLevel(job.getExperienceLevel())
                .status(job.getStatus())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .skills(skillResponses)
                .build();
    }

    private JobEvent toJobEvent(String eventType, Job job) {
        return JobEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .jobId(job.getId())
                .companyId(job.getCompanyId())
                .title(job.getTitle())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .benefits(job.getBenefits())
                .salaryMin(job.getSalaryMin() != null ? job.getSalaryMin().longValue() : null)
                .salaryMax(job.getSalaryMax() != null ? job.getSalaryMax().longValue() : null)
                .currency(job.getCurrency())
                .location(job.getLocation())
                .industry(job.getIndustry())
                .workMode(job.getWorkMode() != null ? job.getWorkMode().name() : null)
                .minYearsExp(job.getMinYearsExp())
                .deadline(job.getDeadline())
                .status(job.getStatus() != null ? job.getStatus().name() : null)
                .timestamp(OffsetDateTime.now())
                .skills(job.getJobSkills() == null ? null : job.getJobSkills().stream()
                        .map(js -> JobEvent.SkillItem.builder()
                                .skillName(js.getSkill().getName())
                                .requiredLevel(js.getRequiredLevel())
                                .mustHave(js.getMustHave())
                                .build())
                        .toList())
                .build();
    }
}