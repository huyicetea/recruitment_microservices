package com.smarthr.job.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSkillReq {

    @NotBlank(message = "Skill name is required")
    private String skill;

    private boolean mustHave;

    private boolean required;
}