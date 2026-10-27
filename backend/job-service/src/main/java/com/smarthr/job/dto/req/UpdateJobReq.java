package com.smarthr.job.dto.req;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

import com.smarthr.job.model.enums.EmploymentType;
import com.smarthr.job.model.enums.ExperienceLevel;
import com.smarthr.job.model.enums.JobStatus;
import com.smarthr.job.model.enums.WorkMode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
public class UpdateJobReq {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    private String requirements;

    private String benefits;

    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum salary cannot be negative")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0.0", inclusive = true, message = "Maximum salary cannot be negative")
    private BigDecimal salaryMax;

    private String currency;

    private String location;

    private String industry;

    private WorkMode workMode;

    @Min(value = 0, message = "Minimum years of experience cannot be negative")
    private Integer minYearsExp;

    @Future(message = "Deadline must be in the future")
    private OffsetDateTime deadline;

    private EmploymentType employmentType;

    private ExperienceLevel experienceLevel;

    private JobStatus status;

    @Valid
    private Set<JobSkillReq> skills;
}