package epita_cloud.com.backend.epita_cloud.base.persistence;

import epita_cloud.com.backend.epita_cloud.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private static final String TABLE_NAME = "users";

    private final DynamoDbClient dynamoDbClient;

    public Optional<User> findByUsername(String username) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":username", AttributeValue.builder().s(username).build());

        ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("username = :username")
                .expressionAttributeValues(expressionValues)
                .build();

        ScanResponse response = dynamoDbClient.scan(request);

        return response.items().stream()
                .findFirst()
                .map(this::toUser);
    }

    public void save(User user) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("user_id", AttributeValue.builder().s(user.getUserId()).build());
        item.put("username", AttributeValue.builder().s(user.getUsername()).build());
        item.put("password", AttributeValue.builder().s(user.getPassword()).build());

        dynamoDbClient.putItem(
                PutItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .item(item)
                        .build()
        );
    }

    private User toUser(Map<String, AttributeValue> item) {
        return User.builder()
                .userId(item.get("user_id").s())
                .username(item.get("username").s())
                .password(item.get("password").s())
                .build();
    }
}