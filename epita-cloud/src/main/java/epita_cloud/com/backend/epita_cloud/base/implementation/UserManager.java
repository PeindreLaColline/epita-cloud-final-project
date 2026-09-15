package epita_cloud.com.backend.epita_cloud.base.implementation;

import epita_cloud.com.backend.epita_cloud.base.business.dto.LoginReq;
import epita_cloud.com.backend.epita_cloud.base.business.dto.LoginResp;
import epita_cloud.com.backend.epita_cloud.base.persistence.UserRepository;
import epita_cloud.com.backend.epita_cloud.entity.User;
import epita_cloud.com.backend.epita_cloud.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserManager {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginResp login(LoginReq loginReq) {
        User user = userRepository.findByUsername(loginReq.username())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(loginReq.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtProvider.generateToken(user.getUserId(), user.getUsername());
        return new LoginResp(user.getUserId(), user.getUsername(), token);
    }
}