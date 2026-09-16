package epita_cloud.com.backend.epita_cloud.base.presentation;

import epita_cloud.com.backend.epita_cloud.base.business.RoomUseCase;
import epita_cloud.com.backend.epita_cloud.base.business.dto.RoomResp;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final RoomUseCase roomUseCase;

    @Operation(
            summary = "get current state of all rooms",
            description = "get the latest occupancy and energy consumption per room"
    )
    @GetMapping()
    public List<RoomResp> getRoomStates() {
        return roomUseCase.getRoomStates();
    }

    @Operation(
            summary = "get current state of a room",
            description = "get the latest occupancy and energy consumption for a single room"
    )
    @GetMapping("/{roomId}")
    public RoomResp getRoomState(@PathVariable String roomId) {
        return roomUseCase.getRoomState(roomId);
    }
}
