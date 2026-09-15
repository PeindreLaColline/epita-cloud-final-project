package epita_cloud.com.backend.epita_cloud.entity;

import lombok.*;

@Getter
@Builder
public class Event {

    private String eventId;
    private String building;
    private String room;
    private String eventType;
    private Integer value;
    private String unit;
    private String severity;
    private String timestamp;

}
