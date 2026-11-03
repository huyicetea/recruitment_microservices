package com.smarthr.auth.service;

import com.smarthr.auth.dto.req.LoginReq;
import com.smarthr.auth.dto.req.RegisterReq;
import com.smarthr.auth.dto.res.AuthRes;

public interface AuthService {
    void register(RegisterReq req);
    AuthRes login(LoginReq req);
}
