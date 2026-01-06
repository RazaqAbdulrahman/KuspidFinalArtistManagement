package com.kuspid.artist.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailClient {

    private final RestTemplate restTemplate;

    @Value("${services.email.url}")
    private String emailServiceUrl;

    public void sendEmail(String to, String subject, String template, Map<String, Object> data) {
        String url = emailServiceUrl + "/api/emails/send";
        EmailRequest request = new EmailRequest();
        request.setTo(to);
        request.setSubject(subject);
        request.setTemplate_name(template);
        request.setTemplate_data(data);

        try {
            restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            log.error("Failed to queue email to {}: {}", to, e.getMessage());
        }
    }

    @Data
    public static class EmailRequest {
        private String to;
        private String subject;
        private String template_name;
        private Map<String, Object> template_data;
    }
}
