package com.smarthr.common.event;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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
public class JobEvent {
    private UUID eventId;
    private String eventType; 
    private UUID jobId;
    private UUID companyId;
    private String title;
    private String description;
    private String requirements;
    private String benefits;
    private Long salaryMin;
    private Long salaryMax;
    private String currency;
    private String location;
    private String industry;
    private String workMode;
    private Integer minYearsExp;
    private OffsetDateTime deadline;
    private String status;
    private List<SkillItem> skills;
    private OffsetDateTime timestamp;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SkillItem {
        private String skillName;
        private String requiredLevel;
        private Boolean mustHave;
    }
}