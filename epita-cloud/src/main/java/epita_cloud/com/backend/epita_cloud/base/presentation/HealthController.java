package epita_cloud.com.backend.epita_cloud.base.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unauthenticated health check used by the Application Load Balancer's target group.
 * Must stay outside Cognito auth (see SecurityConfig permitAll list) since the ALB
 * cannot present a JWT.
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
