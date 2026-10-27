package com.smarthr.job.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.smarthr.common.event.JobEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobEventPublisher {

    private static final String TOPIC = "job.events";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String eventType, JobEvent event) {
        event.setEventType(eventType);
        kafkaTemplate.send(TOPIC, event.getJobId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish {} event for jobId: {}. Error: {}", 
                                eventType, event.getJobId(), ex.getMessage());
                    } else {
                        log.info("Successfully published {} event for jobId: {}", 
                                eventType, event.getJobId());
                    }
                });
    }
}