package com.smarthr.auth.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smarthr.auth.model.entity.Company;

@Repository
public interface CompanyRepo extends JpaRepository<Company, UUID> {
    boolean existsByTaxCode(String taxCode);
}