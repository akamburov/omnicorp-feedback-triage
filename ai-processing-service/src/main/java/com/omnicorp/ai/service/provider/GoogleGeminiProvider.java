package com.omnicorp.ai.service.provider;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnicorp.ai.dto.TriageResponse;
import com.omnicorp.ai.model.FeedbackCategory;
import com.omnicorp.ai.model.FeedbackPriority;
import com.omnicorp.ai.model.Sentiment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component("googleGeminiProvider")
@Order(2)
public class GoogleGeminiProvider implements AiProviderStrategy {

    private static final Logger log = LoggerFactory.getLogger(GoogleGeminiProvider.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String baseUrl;

    @Value("${gemini.api.model:gemini-3.5-flash-lite}")
    private String model;

    private final RestClient restClient;
    private final ObjectMapper mapper;

    // Java Records for Google Gemini API REST JSON Mapping
    public record GeminiResponse(
        @JsonProperty("candidates") List<Candidate> candidates
    ) {
        public record Candidate(
            @JsonProperty("content") CandidateContent content
        ) {}

        public record CandidateContent(
            @JsonProperty("parts") List<Part> parts
        ) {}

        public record Part(
            @JsonProperty("text") String text
        ) {}
    }

    public record GeminiTriagePayload(
        @JsonProperty("category") String category,
        @JsonProperty("priority") String priority,
        @JsonProperty("sentiment") String sentiment,
        @JsonProperty("summary") String summary,
        @JsonProperty("actionableSteps") String actionableSteps
    ) {}

    public GoogleGeminiProvider(RestClient.Builder restClientBuilder, ObjectMapper mapper) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000); // 3 seconds connect timeout
        requestFactory.setReadTimeout(12000);   // 12 seconds read timeout

        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .build();
        this.mapper = mapper;
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.startsWith("YOUR_");
    }

    @Override
    public TriageResponse analyze(String submissionId, String content) {
        if (!isAvailable()) {
            throw new IllegalStateException("Google Gemini API key is not configured.");
        }

        String systemPrompt = """
            You are an expert HR and Workplace Feedback Triage Assistant for OmniCorp Solutions.
            Analyze the following employee feedback and return a strictly valid JSON object matching this structure:
            {
              "category": "FACILITIES" | "IT" | "HR" | "MANAGEMENT" | "OPERATIONAL" | "OTHER",
              "priority": "LOW" | "MEDIUM" | "HIGH" | "CRITICAL",
              "sentiment": "POSITIVE" | "NEUTRAL" | "NEGATIVE",
              "summary": "A concise 1-2 sentence summary of the issue",
              "actionableSteps": "Specific recommended next action for HR or department lead"
            }
            Do not include markdown code block formatting or any text outside the raw JSON object.
            """;

        String combinedPrompt = systemPrompt + "\n\nEmployee Feedback Content:\n" + content;

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(Map.of("text", combinedPrompt)))
            ),
            "generationConfig", Map.of(
                "responseMimeType", "application/json",
                "temperature", 0.2
            )
        );

        String endpointUrl = baseUrl.endsWith("/") ? baseUrl + model + ":generateContent" : baseUrl + "/" + model + ":generateContent";

        try {
            String rawResponse = restClient.post()
                    .uri(endpointUrl)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            GeminiResponse geminiResponse = mapper.readValue(rawResponse, GeminiResponse.class);

            if (geminiResponse == null || geminiResponse.candidates() == null || geminiResponse.candidates().isEmpty()) {
                throw new IllegalStateException("Google Gemini API returned an empty candidates array.");
            }

            GeminiResponse.Candidate candidate = geminiResponse.candidates().get(0);
            if (candidate == null || candidate.content() == null || candidate.content().parts() == null || candidate.content().parts().isEmpty()) {
                throw new IllegalStateException("Google Gemini API candidate content parts are missing.");
            }

            String aiText = candidate.content().parts().get(0).text();
            if (aiText == null || aiText.isBlank()) {
                throw new IllegalStateException("Google Gemini API candidate text is blank.");
            }

            aiText = aiText.replaceAll("```json", "").replaceAll("```", "").trim();
            GeminiTriagePayload payload = mapper.readValue(aiText, GeminiTriagePayload.class);

            return new TriageResponse(
                    submissionId,
                    parseEnum(payload.category(), FeedbackCategory.OTHER, FeedbackCategory.class),
                    parseEnum(payload.priority(), FeedbackPriority.MEDIUM, FeedbackPriority.class),
                    parseEnum(payload.sentiment(), Sentiment.NEUTRAL, Sentiment.class),
                    payload.summary() != null ? payload.summary() : "Feedback recorded.",
                    payload.actionableSteps() != null ? payload.actionableSteps() : "Review and route appropriately.",
                    "Google-" + model
            );

        } catch (Exception e) {
            log.error("Google Gemini provider analysis failed for submission ID {}: {}", submissionId, e.getMessage());
            throw new RuntimeException("Google Gemini API call failed: " + e.getMessage(), e);
        }
    }

    private <E extends Enum<E>> E parseEnum(String val, E fallback, Class<E> enumClass) {
        if (val == null) return fallback;
        try {
            return Enum.valueOf(enumClass, val.toUpperCase().trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
