package com.omnicorp.submission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.omnicorp.submission.model.FeedbackCategory;
import com.omnicorp.submission.model.FeedbackPriority;
import com.omnicorp.submission.model.Sentiment;

public record TriageResponseDto(
    @JsonProperty("submissionId")
    String submissionId,

    @JsonProperty("category")
    FeedbackCategory category,

    @JsonProperty("priority")
    FeedbackPriority priority,

    @JsonProperty("sentiment")
    Sentiment sentiment,

    @JsonProperty("summary")
    String summary,

    @JsonProperty("actionableSteps")
    String actionableSteps,

    @JsonProperty("processedBy")
    String processedBy
) {}
