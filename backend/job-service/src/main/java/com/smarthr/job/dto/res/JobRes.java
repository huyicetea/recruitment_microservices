package com.smarthr.job.dto.res;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import com.smarthr.job.model.enums.EmploymentType;
import com.smarthr.job.model.enums.ExperienceLevel;
import com.smarthr.job.model.enums.JobStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRes {

    private UUID id;
    private UUID companyId;
    private UUID recruiterId;
    private String title;
    private String description;
    private String requirements;
    private String benefits;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;
    private String location;
    private EmploymentType employmentType;
    private ExperienceLevel experienceLevel;
    private JobStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Set<JobSkillRes> skills;
}