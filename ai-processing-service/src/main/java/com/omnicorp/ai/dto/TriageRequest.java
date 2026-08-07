package com.omnicorp.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TriageRequest(
    @NotBlank(message = "Submission ID is required")
    @JsonProperty("submissionId")
    String submissionId,

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 2000, message = "Feedback content must not exceed 2000 characters")
    @JsonProperty("content")
    String content
) {}
