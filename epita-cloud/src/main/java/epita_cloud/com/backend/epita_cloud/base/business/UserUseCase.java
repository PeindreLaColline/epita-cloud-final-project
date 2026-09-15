package epita_cloud.com.backend.epita_cloud.base.business;

import epita_cloud.com.backend.epita_cloud.base.business.dto.LoginReq;
import epita_cloud.com.backend.epita_cloud.base.business.dto.LoginResp;

public interface UserUseCase {
    LoginResp login(LoginReq loginReq);
}
