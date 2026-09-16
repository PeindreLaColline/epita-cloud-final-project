package epita_cloud.com.backend.epita_cloud.base.business.dto;

public record RoomResp(
        String roomId,
        String building,
        String room,
        Integer currentOccupancy,
        String lastUpdated
) {
}
