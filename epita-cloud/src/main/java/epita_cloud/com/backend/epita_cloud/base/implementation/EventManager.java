package epita_cloud.com.backend.epita_cloud.base.implementation;

import epita_cloud.com.backend.epita_cloud.base.business.dto.AddEventReq;
import epita_cloud.com.backend.epita_cloud.base.business.dto.EventStatsResp;
import epita_cloud.com.backend.epita_cloud.base.persistence.EventRepository;
import epita_cloud.com.backend.epita_cloud.entity.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class EventManager {

    private static final Set<String> ABNORMAL_SEVERITIES = Set.of("warning", "critical");

    private final EventRepository eventRepository;

    public Event addEvent(AddEventReq addEventReq) {
        Event event = Event.builder()
                .eventId(UUID.randomUUID().toString())
                .building(addEventReq.building())
                .room(addEventReq.room())
                .eventType(addEventReq.eventType())
                .value(addEventReq.value())
                .unit(addEventReq.unit())
                .severity(addEventReq.severity())
                .timestamp(Instant.now().toString())
                .build();

        eventRepository.save(event);
        return event;
    }

    public List<Event> getRecentEvents() {
        return eventRepository.findAll().stream()
                .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                .limit(10)
                .toList();
    }

    public List<Event> getAbnormalEvents() {
        return eventRepository.findAll().stream()
                .filter(event -> ABNORMAL_SEVERITIES.contains(event.getSeverity().toLowerCase()))
                .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                .limit(10)
                .toList();
    }

    public EventStatsResp getStatistics() {
        List<Event> events = eventRepository.findAll();
        long total = events.size();

        Map<String, Long> bySeverity = events.stream()
                .collect(Collectors.groupingBy(Event::getSeverity, Collectors.counting()));

        Map<String, Long> byBuilding = events.stream()
                .collect(Collectors.groupingBy(Event::getBuilding, Collectors.counting()));

        Map<String, Long> byEventType = events.stream()
                .collect(Collectors.groupingBy(Event::getEventType, Collectors.counting()));

        long abnormalCount = events.stream()
                .filter(event -> ABNORMAL_SEVERITIES.contains(event.getSeverity()))
                .count();

        double abnormalRate = total == 0 ? 0.0 : (double) abnormalCount / total;

        return new EventStatsResp(total, bySeverity, byBuilding, byEventType, abnormalRate);
    }
}
