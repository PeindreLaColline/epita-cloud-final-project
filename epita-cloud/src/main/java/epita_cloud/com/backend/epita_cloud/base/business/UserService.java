package epita_cloud.com.backend.epita_cloud.base.business;

import epita_cloud.com.backend.epita_cloud.base.business.dto.LoginReq;
import epita_cloud.com.backend.epita_cloud.base.business.dto.LoginResp;
import epita_cloud.com.backend.epita_cloud.base.implementation.UserManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserManager userManager;

    @Override
    public LoginResp login(LoginReq loginReq) {
        return userManager.login(loginReq);
    }
}