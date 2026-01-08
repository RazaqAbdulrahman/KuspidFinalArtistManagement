package com.kuspid.analytics.service;

import com.kuspid.analytics.model.Event;
import com.kuspid.analytics.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EventRepository repository;

    public Event logEvent(Event event) {
        if (event.getEventType() == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        log.info("Logging event: {} for resource: {}", event.getEventType(), event.getResourceId());
        return repository.save(event);
    }

    public Map<String, Long> getBeatStats(String beatId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("plays", repository.countByEventTypeAndResourceId(Event.EventType.PLAY, beatId));
        stats.put("downloads", repository.countByEventTypeAndResourceId(Event.EventType.DOWNLOAD, beatId));
        stats.put("shares", repository.countByEventTypeAndResourceId(Event.EventType.SHARE, beatId));
        return stats;
    }

    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalEvents", repository.count());
        dashboard.put("totalPlays", repository.countByEventType(Event.EventType.PLAY));
        dashboard.put("totalDownloads", repository.countByEventType(Event.EventType.DOWNLOAD));
        dashboard.put("appOpens", repository.countByEventType(Event.EventType.APP_OPEN));
        return dashboard;
    }
}
