package epita_cloud.com.backend.epita_cloud.base.business.dto;

public record CampusOccupancyResp(
        String building,
        int totalOccupancy
) {
}
