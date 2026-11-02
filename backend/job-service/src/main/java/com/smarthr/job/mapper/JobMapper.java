package com.smarthr.job.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.smarthr.job.dto.req.JobReq;
import com.smarthr.job.dto.req.JobSkillReq;
import com.smarthr.job.dto.res.JobRes;
import com.smarthr.job.dto.res.JobSkillRes;
import com.smarthr.job.model.entity.Job;
import com.smarthr.job.model.entity.JobSkill;

public class JobMapper {

    public static Job toEntity(JobReq req) {
        Job job = Job.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .level(req.getLevel())
                .jobType(req.getJobType())
                .workMode(req.getWorkMode())
                .location(req.getLocation())
                .industry(req.getIndustry())
                .salaryMin(req.getSalaryMin())
                .salaryMax(req.getSalaryMax())
                .minYearsExp(req.getMinYearsExp() != null ? req.getMinYearsExp() : 0)
                .deadline(req.getDeadline())
                .build();

        if (req.getSkills() != null) {
            List<JobSkill> skills = req.getSkills().stream()
                    .map(s -> toSkillEntity(s, job))
                    .collect(Collectors.toList());
            job.setSkills(skills);
        }
        return job;
    }

    public static JobSkill toSkillEntity(JobSkillReq req, Job job) {
        return JobSkill.builder()
                .job(job)
                .skill(req.getSkill().toLowerCase().trim())
                .mustHave(req.isMustHave())
                .required(req.isRequired())
                .build();
    }

    public static JobRes toRes(Job job) {
        List<JobSkillRes> skillResList = null;
        if (job.getSkills() != null) {
            skillResList = job.getSkills().stream()
                    .map(s -> new JobSkillRes(s.getId(), s.getSkill(), s.isMustHave(), s.isRequired()))
                    .collect(Collectors.toList());
        }

        return JobRes.builder()
                .id(job.getId())
                .companyId(job.getCompanyId())
                .createdBy(job.getCreatedBy())
                .title(job.getTitle())
                .description(job.getDescription())
                .level(job.getLevel())
                .jobType(job.getJobType())
                .workMode(job.getWorkMode())
                .location(job.getLocation())
                .industry(job.getIndustry())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .minYearsExp(job.getMinYearsExp())
                .deadline(job.getDeadline())
                .status(job.getStatus())
                .version(job.getVersion())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .skills(skillResList)
                .build();
    }
}