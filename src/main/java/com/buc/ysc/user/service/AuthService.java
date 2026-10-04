package com.buc.ysc.user.service;


import com.buc.ysc.user.vo.record.UserIdCheckResponse;
import com.buc.ysc.user.vo.reponse.LoginResponse;
import com.buc.ysc.user.vo.reponse.SignupResponse;
import com.buc.ysc.user.vo.record.LoginRequest;
import com.buc.ysc.user.vo.record.SignupRequest;

public interface AuthService {

    SignupResponse signup(SignupRequest request);

    LoginResponse login(LoginRequest request);

    UserIdCheckResponse checkUserId(String userId);
}