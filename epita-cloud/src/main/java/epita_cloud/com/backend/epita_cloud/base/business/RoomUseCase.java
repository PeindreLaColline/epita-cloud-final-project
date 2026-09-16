package epita_cloud.com.backend.epita_cloud.base.business;

import epita_cloud.com.backend.epita_cloud.base.business.dto.CampusOccupancyResp;
import epita_cloud.com.backend.epita_cloud.base.business.dto.RoomResp;

import java.util.List;

public interface RoomUseCase {
    List<RoomResp> getRoomStates();
    RoomResp getRoomState(String roomId);
    List<CampusOccupancyResp> getCampusOccupancy();
}
