package com.omnicorp.submission.client;

import com.omnicorp.submission.dto.TriageResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class AiServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);

    @Value("${ai.service.url:http://localhost:8081/api/v1/triage}")
    private String aiServiceUrl;

    private final RestClient restClient;

    public AiServiceClient(RestClient.Builder restClientBuilder) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000); // 3 seconds
        requestFactory.setReadTimeout(15000);   // 15 seconds

        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .build();
    }

    public TriageResponseDto requestTriage(String submissionId, String content) {
        log.info("Sending triage request to AI Processing Service at: {}", aiServiceUrl);

        Map<String, String> payload = Map.of(
                "submissionId", submissionId,
                "content", content
        );

        try {
            return restClient.post()
                    .uri(aiServiceUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(TriageResponseDto.class);
        } catch (Exception e) {
            log.error("Failed to connect or communicate with AI Service: {}", e.getMessage());
            throw e;
        }
    }
}
