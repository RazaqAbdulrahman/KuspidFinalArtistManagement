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
    public AiResponse analyzeBeat(String assetId) {
        log.info("Requesting analysis for {}", assetId);
        String url = aiServiceUrl + "/api/ai/analyze/full";
        AnalysisRequest request = new AnalysisRequest(assetId);
        return restTemplate.postForObject(url, request, AiResponse.class);
    }

    public AiResponse fallbackAnalyze(String assetId, Throwable t) {
        log.warn("AI Service unavailable for {}. Error: {}", assetId, t.getMessage());
        AiResponse response = new AiResponse();
        response.setStatus("DEGRADED");
        return response;
    }

    @Data
    @RequiredArgsConstructor
    public static class AnalysisRequest {
        private final String assetId;
    }

    @Data
    public static class AiResponse {
        private Integer bpm;
        private String key;
        private String genre;
        private String status;
    }
}
