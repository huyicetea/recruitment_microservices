package com.smarthr.job.dto.res;

import java.time.OffsetDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorRes {
    private int status;
    private String error;
    private String message;
    private Map<String, String> validationErrors;
    private OffsetDateTime timestamp;
}