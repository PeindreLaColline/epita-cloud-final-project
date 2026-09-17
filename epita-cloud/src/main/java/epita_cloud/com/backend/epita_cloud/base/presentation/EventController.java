package epita_cloud.com.backend.epita_cloud.base.presentation;

import epita_cloud.com.backend.epita_cloud.base.business.EventUseCase;
import epita_cloud.com.backend.epita_cloud.base.business.RoomUseCase;
import epita_cloud.com.backend.epita_cloud.base.business.dto.AddEventReq;
import epita_cloud.com.backend.epita_cloud.base.business.dto.CampusOccupancyResp;
import epita_cloud.com.backend.epita_cloud.base.business.dto.EventStatsResp;
import epita_cloud.com.backend.epita_cloud.entity.Event;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventUseCase eventUseCase;
    private final RoomUseCase roomUseCase;

    @Operation(
            summary = "[ADMIN] add Event",
            description = "Add new event one by one"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping()
    public Event addEvent(@RequestBody AddEventReq addEventReq) {
        return eventUseCase.addEvent(addEventReq);
    }

    @Operation(
            summary = "[ADMIN, USER] get recent 10 events",
            description = "get recent 10 events"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping()
    public List<Event> getRecentEvents(){
        return eventUseCase.getRecentEvents();
    }

    @Operation(
            summary = "[ADMIN, USER] Get event statistics",
            description = "Returns statistics for all events, including the total number of events, " +
                    "event counts grouped by severity, building, and event type, " +
                    "as well as the proportion of abnormal events."

    )
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/stats")
    public EventStatsResp getStatistics() {
        return eventUseCase.getStatistics();
    }

    @Operation(
            summary = "[ADMIN, USER] get campus occupancy",
            description = "Aggregates the current total occupancy per building across campus"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/campus-occupancy")
    public List<CampusOccupancyResp> getCampusOccupancy() {
        return roomUseCase.getCampusOccupancy();
    }
}
