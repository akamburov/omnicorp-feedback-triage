package com.omnicorp.ai.service.provider;

import com.omnicorp.ai.dto.TriageResponse;

public interface AiProviderStrategy {
    TriageResponse analyze(String submissionId, String content);
    boolean isAvailable();
}
