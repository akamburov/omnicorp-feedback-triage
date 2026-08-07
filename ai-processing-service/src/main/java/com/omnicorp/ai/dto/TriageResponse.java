package com.omnicorp.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.omnicorp.ai.model.FeedbackCategory;
import com.omnicorp.ai.model.FeedbackPriority;
import com.omnicorp.ai.model.Sentiment;

public record TriageResponse(
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
