package com.kuspid.beat.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiClient {

    private final RestTemplate restTemplate;

    @Value("${services.ai-audio.url}")
    private String aiServiceUrl;

    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackAnalyze")
    public AiResponse analyzeBeat(String s3Key) {
        log.info("Requesting analysis for {}", s3Key);
        String url = aiServiceUrl + "/api/ai/analyze/full";
        AnalysisRequest request = new AnalysisRequest(s3Key);
        return restTemplate.postForObject(url, request, AiResponse.class);
    }

    public AiResponse fallbackAnalyze(String s3Key, Throwable t) {
        log.warn("AI Service unavailable for {}. Error: {}", s3Key, t.getMessage());
        AiResponse response = new AiResponse();
        response.setStatus("DEGRADED");
        return response;
    }

    @Data
    @RequiredArgsConstructor
    public static class AnalysisRequest {
        private final String s3Key;
    }

    @Data
    public static class AiResponse {
        private Integer bpm;
        private String key;
        private String genre;
        private String status;
    }
}
