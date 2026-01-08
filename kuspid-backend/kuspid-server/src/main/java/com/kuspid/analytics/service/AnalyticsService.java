package com.kuspid.analytics.service;

import com.kuspid.analytics.model.Event;
import com.kuspid.analytics.repository.EventRepository;
import com.kuspid.artist.repository.ArtistRepository;
import com.kuspid.beat.repository.BeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EventRepository repository;
    private final ArtistRepository artistRepository;
    private final BeatRepository beatRepository;

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
        dashboard.put("totalArtists", artistRepository.count());
        dashboard.put("totalBeats", beatRepository.count());
        dashboard.put("totalPlays", repository.countByEventType("PLAY"));
        dashboard.put("growthRate", 12.5); // Mocked for now

        List<Map<String, Object>> recent = new ArrayList<>();
        // Mocked recent activity
        recent.add(Map.of("id", "1", "action", "Beat Upload", "target", "Smooth Jazz", "time", "2h ago"));
        recent.add(Map.of("id", "2", "action", "Artist Added", "target", "Lofi Girl", "time", "5h ago"));

        dashboard.put("recentActivity", recent);
        return dashboard;
    }

    public List<Map<String, Object>> getPlaysTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        trend.add(Map.of("name", "Mon", "plays", 400));
        trend.add(Map.of("name", "Tue", "plays", 300));
        trend.add(Map.of("name", "Wed", "plays", 200));
        trend.add(Map.of("name", "Thu", "plays", 500));
        trend.add(Map.of("name", "Fri", "plays", 800));
        trend.add(Map.of("name", "Sat", "plays", 900));
        trend.add(Map.of("name", "Sun", "plays", 1100));
        return trend;
    }

    public List<Map<String, Object>> getGenreDistribution() {
        List<Map<String, Object>> dist = new ArrayList<>();
        dist.add(Map.of("name", "Hip Hop", "value", 40));
        dist.add(Map.of("name", "Jazz", "value", 20));
        dist.add(Map.of("name", "Lofi", "value", 25));
        dist.add(Map.of("name", "Drill", "value", 15));
        return dist;
    }
}
