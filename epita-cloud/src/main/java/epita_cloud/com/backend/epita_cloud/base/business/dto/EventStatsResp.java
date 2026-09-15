package epita_cloud.com.backend.epita_cloud.base.business.dto;

import java.util.Map;

public record EventStatsResp(
        long totalEvents,
        Map<String, Long> bySeverity,
        Map<String, Long> byBuilding,
        Map<String, Long> byEventType,
        double abnormalRate
) {
}