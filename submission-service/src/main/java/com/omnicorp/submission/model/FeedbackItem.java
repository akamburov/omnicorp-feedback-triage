package com.omnicorp.submission.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackItem {
    private String id;
    private String content;
    private String department;
    private String employeeName;
    private TriageStatus status;

    // AI Triage Results
    private FeedbackCategory category;
    private FeedbackPriority priority;
    private Sentiment sentiment;
    private String summary;
    private String actionableSteps;
    private String processedBy;

    private Instant createdAt;
    private Instant updatedAt;
}
