package com.omnicorp.ai.controller;

import com.omnicorp.ai.dto.TriageRequest;
import com.omnicorp.ai.dto.TriageResponse;
import com.omnicorp.ai.service.TriageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/triage")
public class TriageController {

    private final TriageService triageService;

    public TriageController(TriageService triageService) {
        this.triageService = triageService;
    }

    @PostMapping
    public ResponseEntity<TriageResponse> processTriage(@Valid @RequestBody TriageRequest request) {
        TriageResponse response = triageService.triageFeedback(request);
        return ResponseEntity.ok(response);
    }
}
