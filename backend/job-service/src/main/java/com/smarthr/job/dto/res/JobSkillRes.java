package com.smarthr.job.dto.res;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSkillRes {

    private UUID id;
    private String skill;
    private boolean mustHave;
    private boolean required;
}