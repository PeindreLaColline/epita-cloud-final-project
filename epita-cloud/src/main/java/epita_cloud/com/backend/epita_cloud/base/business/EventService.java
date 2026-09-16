package epita_cloud.com.backend.epita_cloud.base.business;


import com.fasterxml.jackson.core.JsonProcessingException;
import epita_cloud.com.backend.epita_cloud.base.business.dto.AddEventReq;
import epita_cloud.com.backend.epita_cloud.base.business.dto.EventStatsResp;
import epita_cloud.com.backend.epita_cloud.base.implementation.EventManager;
import epita_cloud.com.backend.epita_cloud.entity.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService implements EventUseCase {

    private final EventManager eventManager;
    private final ObjectMapper objectMapper;

    @Override
    public Event addEvent(AddEventReq addEventReq) {
        Event event = eventManager.addEvent(addEventReq);

        System.out.println(objectMapper.writeValueAsString(event));

        return event;
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
