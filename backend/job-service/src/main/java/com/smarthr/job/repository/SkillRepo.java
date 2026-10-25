package com.smarthr.job.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smarthr.job.model.entity.Skill;

@Repository
public interface SkillRepo extends JpaRepository<Skill, Integer> {

    Optional<Skill> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    Set<Skill> findByNameInIgnoreCase(Set<String> names);
}