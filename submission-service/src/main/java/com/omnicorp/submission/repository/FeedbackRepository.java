package com.omnicorp.submission.repository;

import com.omnicorp.submission.model.FeedbackItem;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class FeedbackRepository {

    private final Map<String, FeedbackItem> store = new ConcurrentHashMap<>();

    public FeedbackItem save(FeedbackItem item) {
        FeedbackItem.FeedbackItemBuilder builder = item.toBuilder();
        if (item.getId() == null) {
            builder.id(UUID.randomUUID().toString());
        }
        if (item.getCreatedAt() == null) {
            builder.createdAt(Instant.now());
        }
        builder.updatedAt(Instant.now());

        FeedbackItem itemToSave = builder.build();
        store.put(itemToSave.getId(), itemToSave);
        return itemToSave.toBuilder().build(); // Return defensive copy
    }

    public Optional<FeedbackItem> findById(String id) {
        return Optional.ofNullable(store.get(id))
                .map(item -> item.toBuilder().build());
    }

    public List<FeedbackItem> findAll() {
        return store.values().stream()
                .map(item -> item.toBuilder().build())
                .sorted(Comparator.comparing(FeedbackItem::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());
    }
}
