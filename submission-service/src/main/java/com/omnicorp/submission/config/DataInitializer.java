package com.omnicorp.submission.config;

import com.omnicorp.submission.model.*;
import com.omnicorp.submission.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final FeedbackRepository repository;

    public DataInitializer(FeedbackRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing sample feedback dataset...");

        FeedbackItem f1 = FeedbackItem.builder()
                .id("sample-1")
                .content("The AC unit on the 3rd floor office is blowing freezing air directly on our desks. Everyone is shivering.")
                .department("Engineering")
                .employeeName("Alice Smith")
                .status(TriageStatus.TRIAGED)
                .category(FeedbackCategory.FACILITIES)
                .priority(FeedbackPriority.HIGH)
                .sentiment(Sentiment.NEGATIVE)
                .summary("Employee reports severe temperature discomfort on Floor 3 due to AC unit.")
                .actionableSteps("Notify Facilities Maintenance team to inspect site and schedule repair.")
                .processedBy("Mock-RuleEngine-v1")
                .createdAt(Instant.now().minusSeconds(3600))
                .updatedAt(Instant.now().minusSeconds(3600))
                .build();

        FeedbackItem f2 = FeedbackItem.builder()
                .id("sample-2")
                .content("I would like to request a RAM upgrade for my workstation. Compiling the project takes over 15 minutes.")
                .department("Engineering")
                .employeeName("Bob Johnson")
                .status(TriageStatus.TRIAGED)
                .category(FeedbackCategory.IT)
                .priority(FeedbackPriority.MEDIUM)
                .sentiment(Sentiment.NEUTRAL)
                .summary("Developer requesting RAM hardware upgrade to fix slow compile times.")
                .actionableSteps("Create an internal IT Helpdesk ticket and assign to SysAdmin tier 2.")
                .processedBy("Mock-RuleEngine-v1")
                .createdAt(Instant.now().minusSeconds(1800))
                .updatedAt(Instant.now().minusSeconds(1800))
                .build();

        repository.save(f1);
        repository.save(f2);
        log.info("Sample feedback dataset initialized successfully.");
    }
}
