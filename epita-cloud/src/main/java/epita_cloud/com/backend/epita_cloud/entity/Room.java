package epita_cloud.com.backend.epita_cloud.entity;

import lombok.*;

@Getter
@Builder(toBuilder = true)
public class Room {

    private String roomId;
    private String building;
    private String room;
    private Integer currentOccupancy;
    private String lastUpdated;

}
