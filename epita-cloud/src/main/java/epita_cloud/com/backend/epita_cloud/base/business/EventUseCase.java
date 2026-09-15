package epita_cloud.com.backend.epita_cloud.base.business;

import epita_cloud.com.backend.epita_cloud.base.business.dto.AddEventReq;
import epita_cloud.com.backend.epita_cloud.base.business.dto.EventStatsResp;
import epita_cloud.com.backend.epita_cloud.entity.Event;

import java.util.List;


public interface EventUseCase {
    Event addEvent(AddEventReq uploadEventReq);
    List<Event> getRecentEvents();
    List<Event> getAbnormalEvents();
    EventStatsResp getStatistics();
}
