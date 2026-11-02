package com.smarthr.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope<T> {
    private String eventId;
    private String type;
    private Integer version;
    private Instant occurredAt;
    private String producer;
    private String correlationId;
    private T data;
}