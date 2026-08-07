package com.omnicorp.ai.service.provider;

import com.omnicorp.ai.dto.TriageResponse;
import com.omnicorp.ai.model.FeedbackCategory;
import com.omnicorp.ai.model.FeedbackPriority;
import com.omnicorp.ai.model.Sentiment;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component("mockAiProvider")
@Order(3)
public class MockAiProvider implements AiProviderStrategy {

    private static final int MAX_SUMMARY_LENGTH = 120;
    private static final int TRUNCATED_SUMMARY_LENGTH = 117;

    @Override
    public boolean isAvailable() {
        return true; // Mock is always available as final fallback
    }

    @Override
    public TriageResponse analyze(String submissionId, String content) {
        String lowerContent = content.toLowerCase(Locale.ROOT);

        FeedbackCategory category = categorize(lowerContent);
        FeedbackPriority priority = determinePriority(lowerContent);
        Sentiment sentiment = determineSentiment(lowerContent);
        String summary = generateSummary(content);
        String actionableSteps = generateActionableSteps(category, priority);

        return new TriageResponse(
                submissionId,
                category,
                priority,
                sentiment,
                summary,
                actionableSteps,
                "Mock-RuleEngine-v1"
        );
    }

    private FeedbackCategory categorize(String text) {
        if (text.contains("ac") || text.contains("air condition") || text.contains("heating") || text.contains("office") 
                || text.contains("desk") || text.contains("chair") || text.contains("toilet") || text.contains("leak") || text.contains("facility")) {
            return FeedbackCategory.FACILITIES;
        } else if (text.contains("laptop") || text.contains("computer") || text.contains("wifi") || text.contains("network") 
                || text.contains("bug") || text.contains("software") || text.contains("password") || text.contains("it") || text.contains("ram")) {
            return FeedbackCategory.IT;
        } else if (text.contains("salary") || text.contains("pay") || text.contains("benefit") || text.contains("vacation") 
                || text.contains("leave") || text.contains("hr") || text.contains("insurance") || text.contains("health")) {
            return FeedbackCategory.HR;
        } else if (text.contains("manager") || text.contains("lead") || text.contains("team") || text.contains("management") 
                || text.contains("culture") || text.contains("direction") || text.contains("strategy")) {
            return FeedbackCategory.MANAGEMENT;
        } else if (text.contains("process") || text.contains("workflow") || text.contains("shift") || text.contains("schedule")) {
            return FeedbackCategory.OPERATIONAL;
        }
        return FeedbackCategory.OTHER;
    }

    private FeedbackPriority determinePriority(String text) {
        if (text.contains("urgent") || text.contains("critical") || text.contains("immediately") 
                || text.contains("broken") || text.contains("down") || text.contains("crash")) {
            return FeedbackPriority.HIGH;
        } else if (text.contains("slow") || text.contains("issue") || text.contains("problem") || text.contains("cold") || text.contains("hot")) {
            return FeedbackPriority.MEDIUM;
        }
        return FeedbackPriority.LOW;
    }

    private Sentiment determineSentiment(String text) {
        if (text.contains("great") || text.contains("love") || text.contains("good") || text.contains("thanks") || text.contains("awesome")) {
            return Sentiment.POSITIVE;
        } else if (text.contains("bad") || text.contains("terrible") || text.contains("hate") || text.contains("worst") 
                || text.contains("broken") || text.contains("impossible") || text.contains("frustrated")) {
            return Sentiment.NEGATIVE;
        }
        return Sentiment.NEUTRAL;
    }

    private String generateSummary(String original) {
        if (original.length() <= MAX_SUMMARY_LENGTH) {
            return "Feedback summary: " + original;
        }
        return "Feedback summary: " + original.substring(0, TRUNCATED_SUMMARY_LENGTH) + "...";
    }

    private String generateActionableSteps(FeedbackCategory category, FeedbackPriority priority) {
        switch (category) {
            case FACILITIES:
                return "Notify Facilities Maintenance team to inspect site and schedule repair.";
            case IT:
                return "Create an internal IT Helpdesk ticket and assign to SysAdmin tier 2.";
            case HR:
                return "Forward inquiry to HR Benefits & Relations specialist for direct follow-up.";
            case MANAGEMENT:
                return "Escalate summary to Department Head for management review.";
            case OPERATIONAL:
                return "Review operational workflow with team leads during weekly sync.";
            default:
                return "Assign to general triage queue for further manual review.";
        }
    }
}
