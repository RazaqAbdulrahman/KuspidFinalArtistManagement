package com.kuspid.analytics.service;

import com.kuspid.analytics.model.Event;
import com.kuspid.analytics.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
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
    @DisplayName("Log Event - Should save event")
    void logEvent_ShouldSaveEvent() {
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        Event result = analyticsService.logEvent(testEvent);

        assertThat(result.getEventType()).isEqualTo("PLAY");
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("Get Beat Stats - Should return play and download counts")
    void getBeatStats_ShouldReturnCounts() {
        when(eventRepository.countByEventTypeAndResourceId("PLAY", "beat-123")).thenReturn(50L);
        when(eventRepository.countByEventTypeAndResourceId("DOWNLOAD", "beat-123")).thenReturn(10L);

        Map<String, Long> stats = analyticsService.getBeatStats("beat-123");

        assertThat(stats.get("plays")).isEqualTo(50L);
        assertThat(stats.get("downloads")).isEqualTo(10L);
    }

    @Test
    @DisplayName("Get Dashboard Data - Should return total events")
    void getDashboardData_ShouldReturnTotalEvents() {
        when(eventRepository.count()).thenReturn(1000L);

        Map<String, Object> dashboard = analyticsService.getDashboardData();

        assertThat(dashboard.get("totalEvents")).isEqualTo(1000L);
    }
}
