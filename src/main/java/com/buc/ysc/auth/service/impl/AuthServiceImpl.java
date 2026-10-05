package com.buc.ysc.auth.service.impl;

import com.buc.ysc.security.SessionManager;
import com.buc.ysc.security.UserSession;
import com.buc.ysc.user.mapper.UserMapper;
import com.buc.ysc.auth.service.AuthService;
import com.buc.ysc.auth.vo.record.UserIdCheckResponse;
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
        if (userMapper.existsByUserId(request.userId()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, msgUtil.getMsg("AUTH","002"));
        }

        UserVO user = new UserVO();
        user.setUserId(request.userId());
        // 입력받은 비밀번호를 Bcrypt로 암호화
        user.setPasswd(passwordEncoder.encode(request.passwd()));
        user.setUserName(request.userName());
        user.setRole("USER");
        user.setFirstRegEmpNo("TEST0001");
        userMapper.insertUser(user);


        return new SignupResponse(msgUtil.getMsg("AUTH", "005"), request.userId());
    }

    @Override
    public UserIdCheckResponse checkUserId(String userId) {
        int count = userMapper.existsByUserId(userId);

        if (count > 0) {
            return new UserIdCheckResponse(false, msgUtil.getMsg("AUTH", "002"));
        }
        return new UserIdCheckResponse(true, msgUtil.getMsg("AUTH", "003"));
    }

    /**
     * 로그인
     */
    @Override
    public LoginResponse login(LoginRequest request) {

        System.out.println("===== LOGIN START =====");
        System.out.println("userId = " + request.userId());

        // 1. 사용자 조회
        UserVO user = userMapper.selectByUserId(request.userId());

        // 2. 사용자 존재 여부 확인
        if (user == null) {
            System.out.println("USER NOT FOUND");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msgUtil.getMsg("AUTH", "004"));
        }

        // 3. 비밀번호 확인
        boolean passwordMatches = passwordEncoder.matches(request.passwd(),user.getPasswd());

        if (!passwordMatches) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msgUtil.getMsg("AUTH", "004"));
        }

        // 4. Redis 세션 생성
        UserSession session = new UserSession(
                                                user.getUserId(),
                                                user.getUserName(),
                                                user.getRole()
                                        );

        String accessToken = sessionManager.createSession(session);

        // 5. 사용자 정보 생성
        UserInfoResponse userInfo = new UserInfoResponse(
                                                        user.getUserId(),
                                                        user.getUserName(),
                                                        user.getRole()
                                                        );

        // 6. 로그인 응답
        return new LoginResponse(accessToken,"Bearer",sessionManager.getExpiresInSeconds(),userInfo);
    }
}