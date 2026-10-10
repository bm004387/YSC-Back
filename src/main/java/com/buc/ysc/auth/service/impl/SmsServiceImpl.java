package com.buc.ysc.auth.service.impl;

import com.buc.ysc.auth.service.SmsService;
import com.buc.ysc.auth.vo.request.SmsSendRequest;
import com.buc.ysc.auth.vo.request.SmsVerifyRequest;
import com.buc.ysc.auth.vo.response.SignupResponse;
import com.buc.ysc.auth.vo.response.SmsResponse;
import com.buc.ysc.user.mapper.UserMapper;
import com.buc.ysc.util.MsgUtil;
import com.solapi.sdk.message.dto.response.MultipleDetailMessageSentResponse;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SmsServiceImpl implements SmsService {

    private static final String SMS_KEY_PREFIX = "sms:verify:";
    private static final String RECOVERY_KEY_PREFIX = "sms:recovery:";
    private static final long CODE_EXPIRATION_MINUTES = 3;
    private static final Duration RECOVERY_VERIFICATION_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;
    private final DefaultMessageService solapiMessageService;
    private final MsgUtil msgUtil;
    private final UserMapper userMapper;

    @Value("${solapi.sender}")
    private String sender;

    public SmsServiceImpl(StringRedisTemplate redisTemplate, DefaultMessageService solapiMessageService, MsgUtil msgUtil, UserMapper userMapper) {
        this.redisTemplate = redisTemplate;
        this.solapiMessageService = solapiMessageService;
        this.msgUtil = msgUtil;
        this.userMapper = userMapper;
    }

    @Override
    public SmsResponse send(SmsSendRequest request) {

        String hpNo = normalizePhone(request.hpNo());
        String code = generateCode();
        String key = SMS_KEY_PREFIX + hpNo;

        if (userMapper.existsHpNo(hpNo)) {
            return new SmsResponse(false, msgUtil.getMsg("SIGNUP", "005"));
        }

        try {
            // Redis에 인증번호 저장
            redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(CODE_EXPIRATION_MINUTES));

            // SMS 발송
            Message message = new Message();
            message.setFrom(sender);
            message.setTo(hpNo);
            message.setText("[YSC] 인증번호는 " + code + "입니다.");

            MultipleDetailMessageSentResponse response = solapiMessageService.send(message, null);

            return new SmsResponse(true,  msgUtil.getMsg("SMS", "001"));

        } catch (Exception e) {

            // 문자 발송 실패 시 Redis에 저장한 인증번호 삭제
            redisTemplate.delete(key);
            return new SmsResponse(false, msgUtil.getMsg("SMS", "005"));
        }
    }

    @Override
    public SmsResponse verify(SmsVerifyRequest request) {

        String hpNo = normalizePhone(request.hpNo());

        String key = SMS_KEY_PREFIX + hpNo;

        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            return new SmsResponse(false, msgUtil.getMsg("SMS", "002"));
        }

        if (!savedCode.equals(request.code())) {
            return new SmsResponse(false, msgUtil.getMsg("SMS", "003"));
        }

        // 인증 성공 후 인증번호 삭제
        redisTemplate.delete(key);

        return new SmsResponse(true, msgUtil.getMsg("SMS", "004"));
    }

    @Override
    public SmsResponse sendIdRecovery(String phone) {
        String hpNo = normalizePhone(phone);
        if (!userMapper.existsHpNo(hpNo)) {
            return new SmsResponse(false, msgUtil.getMsg("AUTH", "009"));
        }
        return sendRecoveryCode("ID", hpNo, hpNo);
    }

    @Override
    public SmsResponse verifyIdRecovery(String phone, String code) {
        String hpNo = normalizePhone(phone);
        return verifyRecoveryCode("ID", hpNo, hpNo, code);
    }

    @Override
    public SmsResponse sendPasswordRecovery(String usrId, String phone) {
        String hpNo = normalizePhone(phone);
        var user = userMapper.selectByUsrId(usrId);
        if (user == null || !normalizePhone(user.getHpNo()).equals(hpNo)) {
            return new SmsResponse(false, msgUtil.getMsg("AUTH", "010"));
        }
        return sendRecoveryCode("PASSWORD:" + usrId, hpNo, usrId + ":" + hpNo);
    }

    @Override
    public SmsResponse verifyPasswordRecovery(String usrId, String phone, String code) {
        String hpNo = normalizePhone(phone);
        var user = userMapper.selectByUsrId(usrId);
        if (user == null || !normalizePhone(user.getHpNo()).equals(hpNo)) {
            return new SmsResponse(false, msgUtil.getMsg("AUTH", "010"));
        }
        return verifyRecoveryCode("PASSWORD:" + usrId, hpNo, usrId + ":" + hpNo, code);
    }

    @Override
    public boolean consumeIdRecoveryVerification(String phone) {
        return consumeVerification("ID", normalizePhone(phone));
    }

    @Override
    public boolean consumePasswordRecoveryVerification(String usrId, String phone) {
        return consumeVerification("PASSWORD:" + usrId,
                usrId + ":" + normalizePhone(phone));
    }

    private SmsResponse sendRecoveryCode(String purpose, String hpNo, String subject) {
        String codeKey = recoveryCodeKey(purpose, subject);
        String code = generateCode();
        try {
            redisTemplate.opsForValue().set(codeKey, code, Duration.ofMinutes(CODE_EXPIRATION_MINUTES));
            Message message = new Message();
            message.setFrom(sender);
            message.setTo(hpNo);
            message.setText("[YSC] 인증번호는 " + code + "입니다.");
            solapiMessageService.send(message, null);
            return new SmsResponse(true, msgUtil.getMsg("SMS", "001"));
        } catch (Exception e) {
            redisTemplate.delete(codeKey);
            return new SmsResponse(false, msgUtil.getMsg("SMS", "005"));
        }
    }

    private SmsResponse verifyRecoveryCode(String purpose, String hpNo, String subject, String code) {
        String codeKey = recoveryCodeKey(purpose, subject);
        String savedCode = redisTemplate.opsForValue().get(codeKey);
        if (savedCode == null) return new SmsResponse(false, msgUtil.getMsg("SMS", "002"));
        if (!savedCode.equals(code)) return new SmsResponse(false, msgUtil.getMsg("SMS", "003"));
        redisTemplate.delete(codeKey);
        redisTemplate.opsForValue().set(recoveryVerifiedKey(purpose, subject), "Y", RECOVERY_VERIFICATION_TTL);
        return new SmsResponse(true, msgUtil.getMsg("SMS", "004"));
    }

    private boolean consumeVerification(String purpose, String subject) {
        String key = recoveryVerifiedKey(purpose, subject);
        String verified = redisTemplate.opsForValue().get(key);
        if (!"Y".equals(verified)) return false;
        redisTemplate.delete(key);
        return true;
    }

    private String recoveryCodeKey(String purpose, String subject) {
        return RECOVERY_KEY_PREFIX + purpose + ":code:" + subject;
    }

    private String recoveryVerifiedKey(String purpose, String subject) {
        return RECOVERY_KEY_PREFIX + purpose + ":verified:" + subject;
    }

    private String generateCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
    }

    private String normalizePhone(String hpNo) {
        return hpNo == null ? "" : hpNo.replaceAll("[^0-9]", "");
    }
}
