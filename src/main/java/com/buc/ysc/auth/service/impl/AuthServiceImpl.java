package com.buc.ysc.auth.service.impl;

import com.buc.ysc.auth.vo.response.SmsResponse;
import com.buc.ysc.security.SessionManager;
import com.buc.ysc.security.UserSession;
import com.buc.ysc.security.PinLoginThrottle;
import com.buc.ysc.user.mapper.UserMapper;
import com.buc.ysc.auth.service.AuthService;
import com.buc.ysc.auth.vo.record.UsrIdCheckResponse;
import com.buc.ysc.auth.vo.response.LoginResponse;
import com.buc.ysc.auth.vo.response.SignupResponse;
import com.buc.ysc.auth.vo.response.UserInfoResponse;
import com.buc.ysc.auth.vo.record.PinLoginRequest;
import com.buc.ysc.auth.vo.record.PinSetupRequest;
import com.buc.ysc.auth.vo.record.SignupRequest;
import com.buc.ysc.user.vo.request.UserVO;
import com.buc.ysc.util.MsgUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SessionManager sessionManager;
    private final MsgUtil msgUtil;
    private final PinLoginThrottle pinLoginThrottle;


    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder,
                           SessionManager sessionManager, MsgUtil msgUtil,
                           PinLoginThrottle pinLoginThrottle) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.sessionManager = sessionManager;
        this.msgUtil = msgUtil;
        this.pinLoginThrottle = pinLoginThrottle;
    }


    /**
     * 회원가입
     */
    @Override
    public SignupResponse signup(SignupRequest request) {

        // 아이디 중복 확인
        if (userMapper.existsByUsrId(request.usrId()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, msgUtil.getMsg("AUTH","002"));
        }

        UserVO user = new UserVO();
        user.setSystemUserId(request.usrId());
        user.setUsrId(request.usrId());
        // 입력받은 비밀번호를 Bcrypt로 암호화
        user.setPwd(passwordEncoder.encode(request.pwd()));
        user.setUsrNm(request.usrNm());
        user.setRol("USER");
        user.setHpNo(request.hpNo());
        user.setAdr(request.adr());
        user.setDtlAdr(request.dtlAdr());
        userMapper.insertUser(user);


        return new SignupResponse(msgUtil.getMsg("AUTH", "005"), request.usrId());
    }

    @Override
    public UsrIdCheckResponse checkUsrId(String usrId) {
        int count = userMapper.existsByUsrId(usrId);

        if (count > 0) {
            return new UsrIdCheckResponse(false, msgUtil.getMsg("AUTH", "002"));
        }
        return new UsrIdCheckResponse(true, msgUtil.getMsg("AUTH", "003"));
    }

    /** 가입 시 입력한 원래 비밀번호를 확인하고 PIN 해시를 저장합니다. */
    @Override
    public void setupPin(PinSetupRequest request) {
        UserVO user = userMapper.selectByUsrId(request.usrId());
        if (user == null || !passwordEncoder.matches(request.pwd(), user.getPwd())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msgUtil.getMsg("AUTH", "001"));
        }

        user.setSystemUserId(user.getUsrId());
        user.setPinPwd(passwordEncoder.encode(request.pin()));
        if (userMapper.updatePinPassword(user) != 1) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, msgUtil.getMsg("SIGNUP", "004"));
        }
    }

    /** BCrypt PIN을 검증하고 기존 Redis 세션이 있으면 재사용합니다. */
    @Override
    public LoginResponse loginWithPin(PinLoginRequest request, String remoteAddress) {
        if (pinLoginThrottle.isBlocked(request.usrId(), remoteAddress)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    msgUtil.getMsg("AUTH", "006"));
        }

        UserVO user = userMapper.selectByUsrId(request.usrId());
        if (user == null || user.getPinPwd() == null
                || !passwordEncoder.matches(request.pin(), user.getPinPwd())) {
            pinLoginThrottle.recordFailure(request.usrId(), remoteAddress);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msgUtil.getMsg("AUTH", "001"));
        }
        pinLoginThrottle.clearFailures(request.usrId(), remoteAddress);

        UserSession session = new UserSession(
                user.getUsrId(),
                user.getUsrNm(),
                user.getRol(),
                user.getHpNo(),
                user.getAdr(),
                user.getDtlAdr()
        );
        UserSession existingSession = sessionManager.getSession(request.sessionId());
        String accessToken;
        if (existingSession != null && existingSession.usrId().equals(user.getUsrId())) {
            accessToken = request.sessionId();
        } else {
            sessionManager.deleteSession(request.sessionId());
            accessToken = sessionManager.createSession(session, false);
        }
        UserInfoResponse userInfo = new UserInfoResponse(
                user.getUsrId(),
                user.getUsrNm(),
                user.getRol(),
                user.getHpNo(),
                user.getAdr(),
                user.getDtlAdr()
        );
        return new LoginResponse(accessToken, "Bearer", sessionManager.getExpiresInSeconds(false), userInfo);
    }
}
