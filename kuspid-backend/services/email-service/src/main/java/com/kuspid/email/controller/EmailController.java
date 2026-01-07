package com.kuspid.email.controller;

import com.kuspid.email.service.EmailService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
        emailService.sendEmail(request.getTo(), request.getSubject(), request.getText());
        return ResponseEntity.ok("Email send request processed");
    }

    @Data
    public static class EmailRequest {
        private String to;
        private String subject;
        private String text;
    }
}
