package com.buc.ysc.auth.service;


import com.buc.ysc.auth.vo.record.UsrIdCheckResponse;
import com.buc.ysc.auth.vo.response.LoginResponse;
import com.buc.ysc.auth.vo.response.SignupResponse;
import com.buc.ysc.auth.vo.record.SignupRequest;
import com.buc.ysc.auth.vo.record.PinLoginRequest;
import com.buc.ysc.auth.vo.record.PinSetupRequest;
import com.buc.ysc.auth.vo.record.LoginRequest;

public interface AuthService {

    SignupResponse signup(SignupRequest request);

    void setupPin(PinSetupRequest request);

    LoginResponse loginWithPin(PinLoginRequest request, String remoteAddress);

    LoginResponse login(LoginRequest request);

    UsrIdCheckResponse checkUsrId(String usrId);
}
