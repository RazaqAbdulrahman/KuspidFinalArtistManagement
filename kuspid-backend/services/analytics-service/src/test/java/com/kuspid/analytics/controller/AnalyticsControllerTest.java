package com.kuspid.analytics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuspid.analytics.model.Event;
import com.kuspid.analytics.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnalyticsService analyticsService;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        testEvent = Event.builder()
                .id(1L)
                .eventType("PLAY")
                .resourceId("beat-123")
                .userId("user-456")
                .build();
    }

    @Test
    @DisplayName("POST /api/analytics/events - Should log event")
    void logEvent_ShouldReturnCreatedEvent() throws Exception {
        when(analyticsService.logEvent(any(Event.class))).thenReturn(testEvent);

        mockMvc.perform(post("/api/analytics/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventType").value("PLAY"))
                .andExpect(jsonPath("$.resourceId").value("beat-123"));
    }

    @Test
    @DisplayName("GET /api/analytics/beats/{id}/stats - Should return stats")
    void getBeatStats_ShouldReturnStats() throws Exception {
        Map<String, Long> stats = new HashMap<>();
        stats.put("plays", 100L);
        stats.put("downloads", 25L);
        when(analyticsService.getBeatStats("beat-123")).thenReturn(stats);

        mockMvc.perform(get("/api/analytics/beats/beat-123/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plays").value(100))
                .andExpect(jsonPath("$.downloads").value(25));
    }

    @Test
    @DisplayName("GET /api/analytics/dashboard - Should return dashboard data")
    void getDashboardData_ShouldReturnData() throws Exception {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalEvents", 500L);
        when(analyticsService.getDashboardData()).thenReturn(dashboard);

        mockMvc.perform(get("/api/analytics/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents").value(500));
    }
}
