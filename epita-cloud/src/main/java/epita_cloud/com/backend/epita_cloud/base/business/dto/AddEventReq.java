package epita_cloud.com.backend.epita_cloud.base.business.dto;

public record AddEventReq(
        String building,
        String room,
        String eventType,
        Integer value,
        String unit,
        String severity
) {
}
