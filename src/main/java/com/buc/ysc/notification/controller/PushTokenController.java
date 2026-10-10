package com.buc.ysc.notification.controller;

import com.buc.ysc.notification.service.PushNotificationService;
import com.buc.ysc.notification.vo.PushTokenRequest;
import com.buc.ysc.security.SessionManager;
import com.buc.ysc.security.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증된 사용자의 모바일 푸시 토큰 등록과 해제를 처리합니다. */
@RestController
@RequestMapping("/api/notifications/devices")
public class PushTokenController {

    private final SessionManager sessions;
    private final PushNotificationService pushNotificationService;

    public PushTokenController(
            SessionManager sessions,
            PushNotificationService pushNotificationService) {
        this.sessions = sessions;
        this.pushNotificationService = pushNotificationService;
    }

    /** 현재 로그인한 사용자의 기기 토큰을 저장합니다. */
    @PostMapping
    public Map<String, Boolean> register(
            HttpServletRequest request,
            @Valid @RequestBody PushTokenRequest body) {
        UserSession user = session(request);
        pushNotificationService.registerToken(user.usrId(), body.token(), body.platform());
        return Map.of("success", true);
    }

    /** 로그아웃하는 기기의 토큰을 현재 사용자 계정에서 삭제합니다. */
    @DeleteMapping
    public Map<String, Boolean> unregister(
            HttpServletRequest request,
            @Valid @RequestBody PushTokenRequest body) {
        UserSession user = session(request);
        pushNotificationService.unregisterToken(user.usrId(), body.token());
        return Map.of("success", true);
    }

    private UserSession session(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        String token = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : null;
        UserSession user = sessions.getSession(token);
        if (user == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return user;
    }
}
