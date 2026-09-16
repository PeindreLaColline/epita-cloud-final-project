package epita_cloud.com.backend.epita_cloud.base.implementation;

import epita_cloud.com.backend.epita_cloud.base.business.dto.CampusOccupancyResp;
import epita_cloud.com.backend.epita_cloud.base.business.dto.RoomResp;
import epita_cloud.com.backend.epita_cloud.base.persistence.RoomRepository;
import epita_cloud.com.backend.epita_cloud.entity.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class RoomManager {

    private final RoomRepository roomRepository;

    public List<RoomResp> getRoomStates() {
        return roomRepository.findAll().stream()
                .map(this::toResp)
                .toList();
    }

    public RoomResp getRoomState(String roomId) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 room입니다: " + roomId));
        return toResp(room);
    }

    public List<CampusOccupancyResp> getCampusOccupancy() {
        return roomRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Room::getBuilding,
                        Collectors.summingInt(room -> room.getCurrentOccupancy() == null ? 0 : room.getCurrentOccupancy())
                ))
                .entrySet().stream()
                .map(entry -> new CampusOccupancyResp(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CampusOccupancyResp::building))
                .toList();
    }

    private RoomResp toResp(Room room) {
        return new RoomResp(
                room.getRoomId(),
                room.getBuilding(),
                room.getRoom(),
                room.getCurrentOccupancy(),
                room.getLastUpdated()
        );
    }
}
