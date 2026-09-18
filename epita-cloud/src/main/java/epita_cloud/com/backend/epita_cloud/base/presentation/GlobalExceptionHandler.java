package epita_cloud.com.backend.epita_cloud.base.presentation;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns unhandled exceptions into a proper HTTP status + a readable body instead of a bare
 * 500 with no detail. Without this, every failure - a missing room, a bad request, a real bug -
 * looked identical: {"status":500,"error":"Internal Server Error"} with no message.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Repositories/managers throw this for "not found" lookups (e.g. RoomManager.getRoomState).
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // Bad input the app itself rejects.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleBadState(IllegalStateException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // Fallback: anything else still becomes a 500, but now with the real exception class + message
    // (e.g. a NullPointerException from a DynamoDB item missing an expected attribute) so the
    // actual cause is visible in the response instead of only in server-side logs.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex, HttpServletRequest request) {
        String message = ex.getClass().getSimpleName() + (ex.getMessage() != null ? ": " + ex.getMessage() : "");
        return build(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
