package epita_cloud.com.backend.epita_cloud.base.presentation;

import epita_cloud.com.backend.epita_cloud.base.business.EventUseCase;
import epita_cloud.com.backend.epita_cloud.base.business.RoomUseCase;
import epita_cloud.com.backend.epita_cloud.base.business.dto.AddEventReq;
import epita_cloud.com.backend.epita_cloud.base.business.dto.CampusOccupancyResp;
import epita_cloud.com.backend.epita_cloud.base.business.dto.EventStatsResp;
import epita_cloud.com.backend.epita_cloud.entity.Event;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventUseCase eventUseCase;
    private final RoomUseCase roomUseCase;

    @Operation(
            summary = "add Event",
            description = "Add new event one by one"
    )
    @PostMapping()
    public Event addEvent(@RequestBody AddEventReq addEventReq) {
        return eventUseCase.addEvent(addEventReq);
    }

    @Operation(
            summary = "get recent 10 events",
            description = "get recent 10 events"
    )
    @GetMapping()
    public List<Event> getRecentEvents(){
        return eventUseCase.getRecentEvents();
    }

    @Operation(
            summary = "Get event statistics",
            description = "Returns statistics for all events, including the total number of events, " +
                    "event counts grouped by severity, building, and event type, " +
                    "as well as the proportion of abnormal events."

    )
    @GetMapping("/stats")
    public EventStatsResp getStatistics() {
        return eventUseCase.getStatistics();
    }

    @Operation(
            summary = "get campus occupancy",
            description = "Aggregates the current total occupancy per building across campus"
    )
    @GetMapping("/campus-occupancy")
    public List<CampusOccupancyResp> getCampusOccupancy() {
        return roomUseCase.getCampusOccupancy();
    }
}
