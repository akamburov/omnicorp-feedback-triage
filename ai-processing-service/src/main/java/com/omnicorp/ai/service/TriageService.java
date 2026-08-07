package com.omnicorp.ai.service;

import com.omnicorp.ai.dto.TriageRequest;
import com.omnicorp.ai.dto.TriageResponse;
import com.omnicorp.ai.service.provider.AiProviderStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TriageService {

    private static final Logger log = LoggerFactory.getLogger(TriageService.class);

    private final List<AiProviderStrategy> providers;

    public TriageService(List<AiProviderStrategy> providers) {
        this.providers = providers;
    }

    public TriageResponse triageFeedback(TriageRequest request) {
        log.info("Received triage request for submission ID: {}", request.submissionId());

        for (AiProviderStrategy provider : providers) {
            if (provider.isAvailable()) {
                try {
                    log.info("Attempting triage using provider [{}] for submission ID: {}",
                            provider.getClass().getSimpleName(), request.submissionId());
                    return provider.analyze(request.submissionId(), request.content());
                } catch (Exception e) {
                    log.warn("Provider [{}] failed for submission ID {}: {}. Attempting fallback provider...",
                            provider.getClass().getSimpleName(), request.submissionId(), e.getMessage());
                }
            } else {
                log.info("Provider [{}] is not available. Skipping...", provider.getClass().getSimpleName());
            }
        }

        throw new IllegalStateException("No available AI provider could successfully triage submission ID: " + request.submissionId());
    }
}
