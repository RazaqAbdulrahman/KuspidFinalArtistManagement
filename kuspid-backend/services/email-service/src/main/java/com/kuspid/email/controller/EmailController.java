package com.kuspid.email.controller;

import com.kuspid.email.service.EmailService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendEmail(@RequestBody EmailRequest request) {
        // Validation: Ensure basic delivery parameters exist
        if (request.getTo() == null || request.getSubject() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Destination and Subject are immutable requirements"));
        }

        emailService.sendEmail(
                request.getTo(),
                request.getSubject(),
                request.getText(),
                request.isHtml());

        return ResponseEntity.accepted().body(Map.of(
                "status", "QUEUED",
                "message", "Email dispatch delegated to background worker"));
    }

    @Data
    public static class EmailRequest {
        private String to;
        private String subject;
        private String text;
        private boolean isHtml = false;
    }
}
