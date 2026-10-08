package com.buc.ysc.auth.controller;

import com.buc.ysc.security.SessionManager;
import com.buc.ysc.auth.service.AuthService;
import com.buc.ysc.auth.vo.record.UsrIdCheckResponse;
import com.buc.ysc.auth.vo.response.LoginResponse;
import com.buc.ysc.auth.vo.response.SignupResponse;
import com.buc.ysc.auth.vo.response.UserInfoResponse;
import com.buc.ysc.auth.vo.record.LoginRequest;
import com.buc.ysc.auth.vo.record.SignupRequest;
import com.buc.ysc.util.MsgUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionManager sessionManager;
    private final MsgUtil msgUtil;

    public AuthController(AuthService authService, SessionManager sessionManager, MsgUtil msgUtil) {
        this.authService = authService;
        this.sessionManager = sessionManager;
        this.msgUtil = msgUtil;
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @GetMapping("/check-user-id")
    public ResponseEntity<UsrIdCheckResponse> checkUsrId(@RequestParam String usrId) {
        return ResponseEntity.ok(authService.checkUsrId(usrId.trim()));
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** 저장된 access token으로 로그인 세션을 확인합니다. */
    @GetMapping("/session")
    public ResponseEntity<UserInfoResponse> validateSession(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authorization.substring(7);
        var session = sessionManager.getSession(token);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(new UserInfoResponse(
                session.usrId(),
                session.usrNm(),
                session.rol()
        ));
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization != null &&authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            sessionManager.deleteSession(token);
        }

        return ResponseEntity.ok(java.util.Map.of("message",msgUtil.getMsg("AUTH", "002"))
        );
    }
}
