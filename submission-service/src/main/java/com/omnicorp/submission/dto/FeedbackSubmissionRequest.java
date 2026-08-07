package com.omnicorp.submission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeedbackSubmissionRequest(
    @NotBlank(message = "Feedback content cannot be empty")
    @Size(max = 2000, message = "Feedback content must not exceed 2000 characters")
    @JsonProperty("content")
    String content,

    @Size(max = 100, message = "Department name must not exceed 100 characters")
    @JsonProperty("department")
    String department,

    @Size(max = 100, message = "Employee name must not exceed 100 characters")
    @JsonProperty("employeeName")
    String employeeName
) {}
