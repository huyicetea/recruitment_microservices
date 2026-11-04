package com.smarthr.job.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthr.job.dto.req.JobReq;
import com.smarthr.job.dto.req.JobSkillReq;
import com.smarthr.job.model.entity.Job;
import com.smarthr.job.model.entity.Outbox;
import com.smarthr.job.model.enums.JobLevel;
import com.smarthr.job.model.enums.JobStatus;
import com.smarthr.job.model.enums.JobType;
import com.smarthr.job.model.enums.WorkMode;
import com.smarthr.job.repo.JobRepo;
import com.smarthr.job.repo.OutboxRepo;
import com.smarthr.job.scheduler.JobExpirationScheduler;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class JobIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobRepo jobRepo;

    @Autowired
    private OutboxRepo outboxRepo;

    @Autowired
    private JobExpirationScheduler jobExpirationScheduler;

    @BeforeEach
    void setUp() {
        outboxRepo.deleteAll();
        jobRepo.deleteAll();
    }

    @Test
    void shouldCreateJobAndWriteOutboxEvent() throws Exception {
        JobSkillReq skillReq = new JobSkillReq();
        skillReq.setSkill("Java");
        skillReq.setMustHave(true);
        skillReq.setRequired(true);

        JobReq req = new JobReq();
        req.setTitle("Senior Java Developer");
        req.setDescription("Building scalable microservices with Spring Boot and Kafka");
        req.setLevel(JobLevel.SENIOR);
        req.setJobType(JobType.FULL_TIME);
        req.setWorkMode(WorkMode.HYBRID);
        req.setLocation("Hanoi");
        req.setIndustry("IT");
        req.setSalaryMin(20000000L);
        req.setSalaryMax(40000000L);
        req.setMinYearsExp(3);
        req.setDeadline(Instant.now().plus(7, ChronoUnit.DAYS));
        req.setSkills(List.of(skillReq));

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Senior Java Developer"));

        List<Job> jobs = jobRepo.findAll();
        assertThat(jobs).hasSize(1);
        assertThat(jobs.get(0).getStatus()).isEqualTo(JobStatus.OPEN);

        List<Outbox> outboxEvents = outboxRepo.findAll();
        assertThat(outboxEvents).hasSize(1);
        assertThat(outboxEvents.get(0).getType()).isEqualTo("job.created");
    }

    @Test
    void shouldSearchJobsWithFullTextSearch() throws Exception {
        Job job = Job.builder()
                .companyId(UUID.randomUUID())
                .createdBy(UUID.randomUUID())
                .title("React Frontend Developer")
                .description("Expert in TypeScript, Redux, and Tailwind CSS")
                .level(JobLevel.MIDDLE)
                .jobType(JobType.FULL_TIME)
                .workMode(WorkMode.ONSITE)
                .location("Hanoi")
                .industry("IT")
                .salaryMin(15000000L)
                .salaryMax(30000000L)
                .deadline(Instant.now().plus(5, ChronoUnit.DAYS))
                .status(JobStatus.OPEN)
                .build();
        job = jobRepo.save(job);

        mockMvc.perform(get("/jobs")
                        .param("keyword", "TypeScript")
                        .param("industry", "IT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("React Frontend Developer"));
    }

    @Test
    void shouldAutoCloseExpiredJobs() {
        Job expiredJob = Job.builder()
                .companyId(UUID.randomUUID())
                .createdBy(UUID.randomUUID())
                .title("Expired Job")
                .description("This job has expired")
                .level(JobLevel.JUNIOR)
                .jobType(JobType.FULL_TIME)
                .workMode(WorkMode.ONSITE)
                .location("Hanoi")
                .industry("IT")
                .deadline(Instant.now().minus(1, ChronoUnit.DAYS))
                .status(JobStatus.OPEN)
                .build();
        expiredJob = jobRepo.save(expiredJob);

        jobExpirationScheduler.closeExpiredJobs();

        Job updatedJob = jobRepo.findById(expiredJob.getId()).orElseThrow();
        assertThat(updatedJob.getStatus()).isEqualTo(JobStatus.CLOSED);

        List<Outbox> expiredEvents = outboxRepo.findAll().stream()
                .filter(e -> "job.expired".equals(e.getType()))
                .toList();
        assertThat(expiredEvents).hasSize(1);
    }
}