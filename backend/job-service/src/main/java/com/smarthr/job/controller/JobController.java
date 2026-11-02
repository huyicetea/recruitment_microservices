package com.smarthr.job.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smarthr.common.dto.ApiResponse;
import com.smarthr.job.dto.req.JobReq;
import com.smarthr.job.dto.res.JobRes;
import com.smarthr.job.model.enums.JobStatus;
import com.smarthr.job.service.JobService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<ApiResponse<JobRes>> createJob(
            @Valid @RequestBody JobReq req,
            @RequestHeader(value = "X-Company-Id", required = false) UUID companyId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {

        UUID effectiveCompanyId = companyId != null ? companyId : UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID effectiveUserId = userId != null ? userId : UUID.fromString("00000000-0000-0000-0000-000000000001");

        JobRes result = jobService.createJob(req, effectiveCompanyId, effectiveUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Job created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobRes>> updateJob(
            @PathVariable UUID id,
            @Valid @RequestBody JobReq req,
            @RequestHeader(value = "X-Company-Id", required = false) UUID companyId) {

        UUID effectiveCompanyId = companyId != null ? companyId : UUID.fromString("00000000-0000-0000-0000-000000000001");

        JobRes result = jobService.updateJob(id, req, effectiveCompanyId);
        return ResponseEntity.ok(ApiResponse.success(result, "Job updated successfully"));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<ApiResponse<JobRes>> closeJob(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Company-Id", required = false) UUID companyId) {

        UUID effectiveCompanyId = companyId != null ? companyId : UUID.fromString("00000000-0000-0000-0000-000000000001");

        JobRes result = jobService.closeJob(id, effectiveCompanyId);
        return ResponseEntity.ok(ApiResponse.success(result, "Job closed successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobRes>> getJobById(@PathVariable UUID id) {
        JobRes result = jobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Job retrieved successfully"));
    }

    @GetMapping("/company")
    public ResponseEntity<ApiResponse<Page<JobRes>>> getJobsByCompany(
            @RequestHeader(value = "X-Company-Id", required = false) UUID companyId,
            @PageableDefault(size = 10) Pageable pageable) {

        UUID effectiveCompanyId = companyId != null ? companyId : UUID.fromString("00000000-0000-0000-0000-000000000001");

        Page<JobRes> result = jobService.getJobsByCompany(effectiveCompanyId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Company jobs retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobRes>>> searchJobs(
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long salaryMin,
            @RequestParam(required = false) JobStatus status,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<JobRes> result = jobService.searchJobs(industry, location, salaryMin, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Jobs retrieved successfully"));
    }
}