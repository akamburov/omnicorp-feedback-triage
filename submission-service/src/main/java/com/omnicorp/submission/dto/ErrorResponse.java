package com.omnicorp.submission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    @JsonProperty("status")
    int status,

    @JsonProperty("error")
    String error,

    @JsonProperty("message")
    String message,

    @JsonProperty("details")
    List<String> details,

    @JsonProperty("timestamp")
    Instant timestamp
) {}
