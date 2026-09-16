package epita_cloud.com.backend.epita_cloud.base.presentation;

import epita_cloud.com.backend.epita_cloud.base.business.EventUseCase;
import epita_cloud.com.backend.epita_cloud.entity.Event;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alerts")
public class AlertController {

    private final EventUseCase eventUseCase;

    @Operation(
            summary = "get abnormal events",
            description = "get recent 10 abnormal events"
    )
    @GetMapping()
    public List<Event> getAbnormalAlerts(){
        return eventUseCase.getAbnormalEvents();
    }
}
