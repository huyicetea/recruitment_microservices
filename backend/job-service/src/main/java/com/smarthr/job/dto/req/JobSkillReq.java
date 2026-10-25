package com.smarthr.job.dto.req;

import jakarta.validation.constraints.NotBlank;
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
public class JobSkillReq {

    private Integer skillId;

    @NotBlank(message = "Skill name cannot be blank")
    @Size(max = 100, message = "Skill name cannot exceed 100 characters")
    private String skillName;

    @Builder.Default
    private String requiredLevel = "REQUIRED";
}