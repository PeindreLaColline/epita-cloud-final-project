package epita_cloud.com.backend.epita_cloud.base.business;

import epita_cloud.com.backend.epita_cloud.base.business.dto.CampusOccupancyResp;
import epita_cloud.com.backend.epita_cloud.base.business.dto.RoomResp;
import epita_cloud.com.backend.epita_cloud.base.implementation.RoomManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService implements RoomUseCase {

    private final RoomManager roomManager;

    @Override
    public List<RoomResp> getRoomStates() {
        return roomManager.getRoomStates();
    }

    @Override
    public RoomResp getRoomState(String roomId) {
        return roomManager.getRoomState(roomId);
    }

    @Override
    public List<CampusOccupancyResp> getCampusOccupancy() {
        return roomManager.getCampusOccupancy();
    }
}
