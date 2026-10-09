package com.buc.ysc.auth.service.impl;

import com.buc.ysc.auth.vo.response.SmsResponse;
import com.buc.ysc.security.SessionManager;
import com.buc.ysc.security.UserSession;
import com.buc.ysc.user.mapper.UserMapper;
import com.buc.ysc.auth.service.AuthService;
import com.buc.ysc.auth.vo.record.UsrIdCheckResponse;
import com.buc.ysc.auth.vo.response.LoginResponse;
import com.buc.ysc.auth.vo.response.SignupResponse;
import com.buc.ysc.auth.vo.response.UserInfoResponse;
import com.buc.ysc.auth.vo.record.LoginRequest;
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


    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder,
                           SessionManager sessionManager, MsgUtil msgUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.sessionManager = sessionManager;
        this.msgUtil = msgUtil;
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

    /**
     * 로그인
     */
    @Override
    public LoginResponse login(LoginRequest request) {

        System.out.println("===== LOGIN START =====");
        System.out.println("usrId = " + request.usrId());

        // 1. 사용자 조회
        UserVO user = userMapper.selectByUsrId(request.usrId());

        // 2. 사용자 존재 여부 확인
        if (user == null) {
            System.out.println("USER NOT FOUND");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msgUtil.getMsg("AUTH", "001"));
        }

        // 3. 비밀번호 확인
        boolean passwordMatches = passwordEncoder.matches(request.pwd(),user.getPwd());

        if (!passwordMatches) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msgUtil.getMsg("AUTH", "001"));
        }

        // 4. Redis 세션 생성
        UserSession session = new UserSession(
                                                user.getUsrId(),
                                                user.getUsrNm(),
                                                user.getRol(),
                                                user.getHpNo(),
                                                user.getAdr(),
                                                user.getDtlAdr()
                                        );

        String accessToken = sessionManager.createSession(session, request.rememberMe());

        // 5. 사용자 정보 생성
        UserInfoResponse userInfo = new UserInfoResponse(
                                                        user.getUsrId(),
                                                        user.getUsrNm(),
                                                        user.getRol(),
                                                        user.getHpNo(),
                                                        user.getAdr(),
                                                        user.getDtlAdr()
                                                        );

        // 6. 로그인 응답
        return new LoginResponse(accessToken, "Bearer", sessionManager.getExpiresInSeconds(request.rememberMe()), userInfo);
    }
}
