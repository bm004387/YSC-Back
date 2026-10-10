package com.buc.ysc.notification.service;

import com.buc.ysc.notification.mapper.PushTokenMapper;
import com.buc.ysc.common.util.PushNotificationUtil;
import com.buc.ysc.util.MsgUtil;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 기기 푸시 토큰을 저장하고 FCM/APNs 알림을 전송합니다. */
@Service
public class PushNotificationService {

    private final PushTokenMapper mapper;
    private final MsgUtil messages;
    private final PushNotificationUtil pushNotificationUtil;

    public PushNotificationService(
            PushTokenMapper mapper,
            MsgUtil messages,
            PushNotificationUtil pushNotificationUtil) {
        this.mapper = mapper;
        this.messages = messages;
        this.pushNotificationUtil = pushNotificationUtil;
    }

    /** 로그인 사용자의 기기 토큰을 등록합니다. */
    @Transactional
    public void registerToken(String usrId, String token, String platform) {
        mapper.upsertToken(usrId, token, platform.toUpperCase(Locale.ROOT));
    }

    /** 로그아웃한 기기의 푸시 토큰을 현재 계정에서 제거합니다. */
    @Transactional
    public void unregisterToken(String usrId, String token) {
        mapper.deleteToken(usrId, token);
    }

    /** 팔로우 관계가 저장된 뒤 대상 사용자의 모든 등록 기기로 알림을 보냅니다. */
    public void sendNewFollower(String targetUsrId, String followerUsrId, String followerName) {
        String title = messages.getMsg("COMMUNITY", "053");
        String body = messages.getMsg("COMMUNITY", "054").replace("{0}", followerName);
        pushNotificationUtil.sendToUser(
                targetUsrId,
                title,
                body,
                Map.of("type", "NEW_FOLLOWER", "usrId", followerUsrId));
    }
}
