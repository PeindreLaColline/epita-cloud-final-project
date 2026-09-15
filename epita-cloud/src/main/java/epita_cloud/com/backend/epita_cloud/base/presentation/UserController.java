package epita_cloud.com.backend.epita_cloud.base.presentation;

import epita_cloud.com.backend.epita_cloud.base.business.UserUseCase;
import epita_cloud.com.backend.epita_cloud.base.business.dto.LoginReq;
import epita_cloud.com.backend.epita_cloud.base.business.dto.LoginResp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserUseCase userUseCase;

    @PostMapping("/login")
    public LoginResp login(@RequestBody LoginReq loginReq) {
        return userUseCase.login(loginReq);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleLoginError(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }
}
