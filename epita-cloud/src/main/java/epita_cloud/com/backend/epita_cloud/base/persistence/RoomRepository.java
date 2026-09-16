package epita_cloud.com.backend.epita_cloud.base.persistence;

import epita_cloud.com.backend.epita_cloud.entity.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoomRepository {

    private static final String TABLE_NAME = "rooms";

    private final DynamoDbClient dynamoDbClient;

    public Optional<Room> findByRoomId(String roomId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("room_id", AttributeValue.builder().s(roomId).build());

        GetItemResponse response = dynamoDbClient.getItem(
                GetItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .key(key)
                        .build()
        );

        if (!response.hasItem()) {
            return Optional.empty();
        }

        return Optional.of(toRoom(response.item()));
    }

    public List<Room> findAll() {
        ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();

        ScanResponse response = dynamoDbClient.scan(request);
        return response.items()
                .stream()
                .map(this::toRoom)
                .toList();
    }

    public void save(Room room) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("room_id", AttributeValue.builder().s(room.getRoomId()).build());
        item.put("building", AttributeValue.builder().s(room.getBuilding()).build());
        item.put("room", AttributeValue.builder().s(room.getRoom()).build());
        item.put("last_updated", AttributeValue.builder().s(room.getLastUpdated()).build());

        if (room.getCurrentOccupancy() != null) {
            item.put("current_occupancy", AttributeValue.builder().n(String.valueOf(room.getCurrentOccupancy())).build());
        }

        dynamoDbClient.putItem(
                PutItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .item(item)
                        .build()
        );
    }

    private Room toRoom(Map<String, AttributeValue> item) {
        return Room.builder()
                .roomId(item.get("room_id").s())
                .building(item.get("building").s())
                .room(item.get("room").s())
                .currentOccupancy(item.containsKey("current_occupancy") ? Integer.parseInt(item.get("current_occupancy").n()) : null)
                .lastUpdated(item.get("last_updated").s())
                .build();
    }
}
