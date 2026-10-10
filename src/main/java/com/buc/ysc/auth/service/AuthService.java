package com.buc.ysc.auth.service;


import com.buc.ysc.auth.vo.record.UsrIdCheckResponse;
import com.buc.ysc.auth.vo.response.LoginResponse;
import com.buc.ysc.auth.vo.response.SignupResponse;
import com.buc.ysc.auth.vo.record.SignupRequest;
import com.buc.ysc.auth.vo.record.PinLoginRequest;
import com.buc.ysc.auth.vo.record.PinSetupRequest;

public interface AuthService {

    SignupResponse signup(SignupRequest request);

    void setupPin(PinSetupRequest request);

    LoginResponse loginWithPin(PinLoginRequest request, String remoteAddress);

    UsrIdCheckResponse checkUsrId(String usrId);
}
