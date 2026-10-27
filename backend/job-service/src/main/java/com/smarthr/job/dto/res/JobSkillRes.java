package com.smarthr.job.dto.res;

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
public class JobSkillRes {

    private Integer skillId;
    private String skillName;
    private String category;       
    private String requiredLevel;
    private Boolean mustHave;     
}