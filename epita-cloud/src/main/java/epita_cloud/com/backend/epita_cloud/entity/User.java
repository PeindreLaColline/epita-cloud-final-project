package epita_cloud.com.backend.epita_cloud.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {
    private String userId;
    private String username;
    private String password; // BCrypt 해시값
}