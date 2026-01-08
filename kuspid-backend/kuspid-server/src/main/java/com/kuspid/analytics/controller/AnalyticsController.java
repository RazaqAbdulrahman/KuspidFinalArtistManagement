package com.kuspid.analytics.controller;

import com.kuspid.analytics.model.Event;
import com.kuspid.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService service;

    @PostMapping("/events")
    public ResponseEntity<Event> logEvent(@RequestBody Event event) {
        return ResponseEntity.ok(service.logEvent(event));
    }

    @GetMapping("/beats/{id}/stats")
    public ResponseEntity<Map<String, Long>> getBeatStats(@PathVariable String id) {
        return ResponseEntity.ok(service.getBeatStats(id));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        return ResponseEntity.ok(service.getDashboardData());
    }

    @GetMapping("/plays-trend")
    public ResponseEntity<List<Map<String, Object>>> getPlaysTrend() {
        return ResponseEntity.ok(service.getPlaysTrend());
    }

    @GetMapping("/genre-distribution")
    public ResponseEntity<List<Map<String, Object>>> getGenreDistribution() {
        return ResponseEntity.ok(service.getGenreDistribution());
    }
}
