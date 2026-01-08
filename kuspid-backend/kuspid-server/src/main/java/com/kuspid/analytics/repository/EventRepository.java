package com.kuspid.analytics.repository;

import com.kuspid.analytics.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByResourceId(String resourceId);

    long countByEventTypeAndResourceId(String eventType, String resourceId);

    long countByEventType(String eventType);
}
