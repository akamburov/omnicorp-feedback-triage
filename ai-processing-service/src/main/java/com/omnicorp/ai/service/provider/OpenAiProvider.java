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

@Component("openAiProvider")
@Order(1)
public class OpenAiProvider implements AiProviderStrategy {

    private static final Logger log = LoggerFactory.getLogger(OpenAiProvider.class);

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${openai.api.model:gpt-4o-mini}")
    private String model;

    private final RestClient restClient;
    private final ObjectMapper mapper;

    // Mapping OpenAPI/OpenAI JSON field names to Java Record components
    public record OpenAiChatResponse(
        @JsonProperty("choices") List<Choice> choices
    ) {
        public record Choice(
            @JsonProperty("message") Message message
        ) {}

        public record Message(
            @JsonProperty("content") String content
        ) {}
    }

    public record OpenAiTriagePayload(
        @JsonProperty("category") String category,
        @JsonProperty("priority") String priority,
        @JsonProperty("sentiment") String sentiment,
        @JsonProperty("summary") String summary,
        @JsonProperty("actionableSteps") String actionableSteps
    ) {}

    public OpenAiProvider(RestClient.Builder restClientBuilder, ObjectMapper mapper) {
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
            throw new IllegalStateException("OpenAI API key is not configured.");
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

        Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", content)
            ),
            "response_format", Map.of("type", "json_object"),
            "temperature", 0.2
        );

        try {
            String rawResponse = restClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            OpenAiChatResponse chatResponse = mapper.readValue(rawResponse, OpenAiChatResponse.class);

            if (chatResponse == null || chatResponse.choices() == null || chatResponse.choices().isEmpty()) {
                throw new IllegalStateException("OpenAI API returned an empty choices array.");
            }

            OpenAiChatResponse.Choice choice = chatResponse.choices().get(0);
            if (choice == null || choice.message() == null || choice.message().content() == null) {
                throw new IllegalStateException("OpenAI API choice message content is missing.");
            }

            String aiText = choice.message().content().replaceAll("```json", "").replaceAll("```", "").trim();
            OpenAiTriagePayload payload = mapper.readValue(aiText, OpenAiTriagePayload.class);

            return new TriageResponse(
                    submissionId,
                    parseEnum(payload.category(), FeedbackCategory.OTHER, FeedbackCategory.class),
                    parseEnum(payload.priority(), FeedbackPriority.MEDIUM, FeedbackPriority.class),
                    parseEnum(payload.sentiment(), Sentiment.NEUTRAL, Sentiment.class),
                    payload.summary() != null ? payload.summary() : "Feedback recorded.",
                    payload.actionableSteps() != null ? payload.actionableSteps() : "Review and route appropriately.",
                    "OpenAI-" + model
            );

        } catch (Exception e) {
            log.error("OpenAI provider analysis failed for submission ID {}: {}", submissionId, e.getMessage());
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage(), e);
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
