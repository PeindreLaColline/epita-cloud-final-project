package epita_cloud.com.backend.epita_cloud.base.business;

import epita_cloud.com.backend.epita_cloud.base.business.dto.AddEventReq;
import epita_cloud.com.backend.epita_cloud.base.business.dto.EventStatsResp;
import epita_cloud.com.backend.epita_cloud.base.implementation.EventManager;
import epita_cloud.com.backend.epita_cloud.base.persistence.EventRepository;
import epita_cloud.com.backend.epita_cloud.entity.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService implements EventUseCase {

    private final EventManager eventManager;

    @Override
    public Event addEvent(AddEventReq addEventReq) {
        return eventManager.addEvent(addEventReq);
    }

    @Override
    public List<Event> getRecentEvents() {
        return eventManager.getRecentEvents();
    }

    @Override
    public List<Event> getAbnormalEvents() {
        return eventManager.getAbnormalEvents();
    }

    @Override
    public EventStatsResp getStatistics() {
        return eventManager.getStatistics();
    }
}
