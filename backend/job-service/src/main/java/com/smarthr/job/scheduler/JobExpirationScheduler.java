package com.smarthr.job.scheduler;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthr.job.dto.res.JobRes;
import com.smarthr.job.mapper.JobMapper;
import com.smarthr.job.model.entity.Job;
import com.smarthr.job.model.entity.Outbox;
import com.smarthr.job.model.enums.JobStatus;
import com.smarthr.job.repo.JobRepo;
import com.smarthr.job.repo.OutboxRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JobExpirationScheduler {

    private final JobRepo jobRepo;
    private final OutboxRepo outboxRepo;
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void closeExpiredJobs() {
        List<Job> expiredJobs = jobRepo.findByStatusAndDeadlineBefore(JobStatus.OPEN, Instant.now());

        for (Job job : expiredJobs) {
            job.setStatus(JobStatus.CLOSED);
            jobRepo.save(job);

            try {
                JobRes payloadRes = JobMapper.toRes(job);
                String payloadJson = objectMapper.writeValueAsString(payloadRes);

                Outbox outbox = Outbox.builder()
                        .aggregateType("JOB")
                        .aggregateId(job.getId().toString())
                        .type("job.expired")
                        .payload(payloadJson)
                        .build();

                outboxRepo.save(outbox);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing job.expired payload", e);
            }
        }
    }
}