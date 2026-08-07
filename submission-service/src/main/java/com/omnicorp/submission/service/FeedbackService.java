package com.omnicorp.submission.service;

import com.omnicorp.submission.client.AiServiceClient;
import com.omnicorp.submission.dto.FeedbackSubmissionRequest;
import com.omnicorp.submission.dto.TriageResponseDto;
import com.omnicorp.submission.model.FeedbackItem;
import com.omnicorp.submission.model.TriageStatus;
import com.omnicorp.submission.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    private final FeedbackRepository repository;
    private final AiServiceClient aiServiceClient;

    public FeedbackService(FeedbackRepository repository, AiServiceClient aiServiceClient) {
        this.repository = repository;
        this.aiServiceClient = aiServiceClient;
    }

    public FeedbackItem submitFeedback(FeedbackSubmissionRequest request) {
        FeedbackItem item = FeedbackItem.builder()
                .content(request.content())
                .department(request.department() != null && !request.department().isBlank() ? request.department() : "General")
                .employeeName(request.employeeName() != null && !request.employeeName().isBlank() ? request.employeeName() : "Anonymous")
                .status(TriageStatus.PENDING)
                .build();

        FeedbackItem savedItem = repository.save(item);
        log.info("Saved new feedback submission with ID: {}", savedItem.getId());

        // Asynchronously trigger AI Triage processing
        processTriageAsync(savedItem.getId(), savedItem.getContent());

        return savedItem;
    }

    @Async("taskExecutor")
    public void processTriageAsync(String submissionId, String content) {
        log.info("Starting async triage for ID: {}", submissionId);

        try {
            TriageResponseDto triage = aiServiceClient.requestTriage(submissionId, content);

            repository.findById(submissionId).ifPresent(existing -> {
                FeedbackItem updated = existing.toBuilder()
                        .category(triage.category())
                        .priority(triage.priority())
                        .sentiment(triage.sentiment())
                        .summary(triage.summary())
                        .actionableSteps(triage.actionableSteps())
                        .processedBy(triage.processedBy())
                        .status(TriageStatus.TRIAGED)
                        .build();
                repository.save(updated);
                log.info("Successfully updated submission ID: {} with AI triage results", submissionId);
            });

        } catch (Exception e) {
            log.error("Async triage failed for submission ID: {}. Marking status as FAILED.", submissionId, e);
            repository.findById(submissionId).ifPresent(existing -> {
                FeedbackItem failed = existing.toBuilder()
                        .status(TriageStatus.FAILED)
                        .build();
                repository.save(failed);
            });
        }
    }

    public List<FeedbackItem> getAllFeedback() {
        return repository.findAll();
    }

    public Optional<FeedbackItem> getFeedbackById(String id) {
        return repository.findById(id);
    }
}
