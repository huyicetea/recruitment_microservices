package com.smarthr.job.dto.req;

import java.time.Instant;
import java.util.List;

import com.smarthr.job.model.enums.JobLevel;
import com.smarthr.job.model.enums.JobType;
import com.smarthr.job.model.enums.WorkMode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobReq {

    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    @NotNull(message = "Job level is required")
    private JobLevel level;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    @NotNull(message = "Work mode is required")
    private WorkMode workMode;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Industry is required")
    private String industry;

    private Long salaryMin;

    private Long salaryMax;

    private Integer minYearsExp;

    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline must be in the future")
    private Instant deadline;

    @NotEmpty(message = "At least one skill is required")
    @Valid
    private List<JobSkillReq> skills;
}