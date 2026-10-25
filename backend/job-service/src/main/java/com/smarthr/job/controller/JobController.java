package com.smarthr.job.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smarthr.job.dto.req.CreateJobReq;
import com.smarthr.job.dto.req.UpdateJobReq;
import com.smarthr.job.dto.res.JobRes;
import com.smarthr.job.service.JobService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobRes> createJob(@Valid @RequestBody CreateJobReq req) {
        JobRes res = jobService.createJob(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobRes> getJobById(@PathVariable UUID id) {
        JobRes res = jobService.getJobById(id);
        return ResponseEntity.ok(res);
    }

    @GetMapping
    public ResponseEntity<Page<JobRes>> getPublishedJobs(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<JobRes> res = jobService.getPublishedJobs(pageable);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<Page<JobRes>> getJobsByCompany(
            @PathVariable UUID companyId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<JobRes> res = jobService.getJobsByCompany(companyId, pageable);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobRes> updateJob(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateJobReq req) {
        JobRes res = jobService.updateJob(id, req);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}