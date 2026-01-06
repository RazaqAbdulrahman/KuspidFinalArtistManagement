package com.kuspid.analytics.service;

import com.kuspid.analytics.model.Event;
import com.kuspid.analytics.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EventRepository repository;

    public Event logEvent(Event event) {
        return repository.save(event);
    }

    public Map<String, Long> getBeatStats(String beatId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("plays", repository.countByEventTypeAndResourceId("PLAY", beatId));
        stats.put("downloads", repository.countByEventTypeAndResourceId("DOWNLOAD", beatId));
        return stats;
    }

    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalEvents", repository.count());
        // Add more aggregations as needed
        return dashboard;
    }
}
