package com.buc.ysc.auth.service;


import com.buc.ysc.auth.vo.record.UserIdCheckResponse;
import com.buc.ysc.auth.vo.response.LoginResponse;
import com.buc.ysc.auth.vo.response.SignupResponse;
import com.buc.ysc.auth.vo.record.LoginRequest;
import com.buc.ysc.auth.vo.record.SignupRequest;

public interface AuthService {

    SignupResponse signup(SignupRequest request);

    LoginResponse login(LoginRequest request);

    UserIdCheckResponse checkUserId(String userId);
}