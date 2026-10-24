package com.smarthr.job.model.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter 
@Setter 
@Table(name = "job_skills")
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class JobSkill {
    
    @Embeddable 
    @Getter 
    @Setter 
    @NoArgsConstructor 
    @AllArgsConstructor 
    @EqualsAndHashCode 
    public static class JobSkillId implements Serializable {

        @Column(name = "job_id")
        private UUID jobId;

        @Column(name = "skill_id")
        private Integer skillId;
    }

    @EmbeddedId 
    private JobSkillId id;


    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("jobId")
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("skillId")
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "required_level", length = 50)
    @Builder.Default
    private String requiredLevel = "REQUIRED";
}
