package epita_cloud.com.backend.epita_cloud.base.persistence;

import epita_cloud.com.backend.epita_cloud.entity.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class EventRepository {

    private static final String TABLE_NAME = "events";

    private final DynamoDbClient dynamoDbClient;

    public void save(Event event) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("event_id", AttributeValue.builder().s(event.getEventId()).build());
        item.put("room_id", AttributeValue.builder().s(event.getRoomId()).build());
        item.put("building", AttributeValue.builder().s(event.getBuilding()).build());
        item.put("room", AttributeValue.builder().s(event.getRoom()).build());
        item.put("event_type", AttributeValue.builder().s(event.getEventType()).build());
        item.put("value", AttributeValue.builder().n(String.valueOf(event.getValue())).build());
        item.put("unit", AttributeValue.builder().s(event.getUnit()).build());
        item.put("severity", AttributeValue.builder().s(event.getSeverity()).build());
        item.put("timestamp", AttributeValue.builder().s(event.getTimestamp()).build());

        dynamoDbClient.putItem(
                PutItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .item(item)
                        .build()
        );
    }

    public List<Event> findAll() {
        ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();

        ScanResponse response = dynamoDbClient.scan(request);
        return response.items()
                .stream()
                .map(this::toEvent)
                .toList();
    }

    private Event toEvent(Map<String, AttributeValue> item) {
        return Event.builder()
                .eventId(item.get("event_id").s())
                .roomId(item.containsKey("room_id") ? item.get("room_id").s() : null)
                .building(item.get("building").s())
                .room(item.get("room").s())
                .eventType(item.get("event_type").s())
                .value(Integer.parseInt(item.get("value").n()))
                .unit(item.get("unit").s())
                .severity(item.get("severity").s())
                .timestamp(item.get("timestamp").s())
                .build();
    }
}
