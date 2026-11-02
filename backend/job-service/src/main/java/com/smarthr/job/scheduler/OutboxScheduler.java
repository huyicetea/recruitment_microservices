package com.smarthr.job.scheduler;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smarthr.job.model.entity.Outbox;
import com.smarthr.job.repo.OutboxRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxRepo outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void processOutboxEvents() {
        List<Outbox> unprocessedEvents = outboxRepo.findByProcessedFalseOrderByCreatedAtAsc();

        for (Outbox event : unprocessedEvents) {
            try {
                kafkaTemplate.send("job.events", event.getAggregateId(), event.getPayload()).get();
                event.setProcessed(true);
                outboxRepo.save(event);
            } catch (Exception e) {
                break;
            }
        }
    }
}