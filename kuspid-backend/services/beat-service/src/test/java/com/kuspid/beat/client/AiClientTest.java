package com.kuspid.beat.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AiClient aiClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiClient, "aiServiceUrl", "http://localhost:8086");
    }

    @Test
    @DisplayName("Analyze Beat - Should return AI response")
    void analyzeBeat_ShouldReturnAiResponse() {
        AiClient.AiResponse mockResponse = new AiClient.AiResponse();
        mockResponse.setBpm(128);
        mockResponse.setKey("Dm");
        mockResponse.setGenre("House");
        mockResponse.setStatus("COMPLETED");

        when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(mockResponse);

        AiClient.AiResponse result = aiClient.analyzeBeat("test-file.mp3");

        assertThat(result.getBpm()).isEqualTo(128);
        assertThat(result.getKey()).isEqualTo("Dm");
    }

    @Test
    @DisplayName("Fallback Analyze - Should return degraded response on failure")
    void fallbackAnalyze_ShouldReturnDegradedResponse() {
        AiClient.AiResponse result = aiClient.fallbackAnalyze("test-file.mp3",
                new RestClientException("Service unavailable"));

        assertThat(result.getStatus()).isEqualTo("DEGRADED");
    }
}
