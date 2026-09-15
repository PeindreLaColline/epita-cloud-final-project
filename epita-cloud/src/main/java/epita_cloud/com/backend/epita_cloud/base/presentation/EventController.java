package epita_cloud.com.backend.epita_cloud.base.presentation;

import epita_cloud.com.backend.epita_cloud.base.business.EventUseCase;
import epita_cloud.com.backend.epita_cloud.base.business.dto.AddEventReq;
import epita_cloud.com.backend.epita_cloud.base.business.dto.EventStatsResp;
import epita_cloud.com.backend.epita_cloud.entity.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventUseCase eventUseCase;

    @PostMapping()
    public Event addEvent(@RequestBody AddEventReq addEventReq) {
        return eventUseCase.addEvent(addEventReq);
    }

    @GetMapping()
    public List<Event> getRecentEvents(){
        return eventUseCase.getRecentEvents();
    }

    @GetMapping("/stats")
    public EventStatsResp getStatistics() {
        return eventUseCase.getStatistics();
    }
}
