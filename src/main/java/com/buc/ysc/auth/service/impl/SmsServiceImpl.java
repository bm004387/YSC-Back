package com.buc.ysc.auth.service.impl;

import com.buc.ysc.auth.service.SmsService;
import com.buc.ysc.auth.vo.request.SmsSendRequest;
import com.buc.ysc.auth.vo.request.SmsVerifyRequest;
import com.buc.ysc.auth.vo.response.SmsResponse;
import com.buc.ysc.util.MsgUtil;
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
    private static final long CODE_EXPIRATION_MINUTES = 3;

    private final StringRedisTemplate redisTemplate;
    private final DefaultMessageService solapiMessageService;
    private final MsgUtil msgUtil;

    @Value("${solapi.sender}")
    private String sender;

    public SmsServiceImpl(StringRedisTemplate redisTemplate, DefaultMessageService solapiMessageService, MsgUtil msgUtil) {
        this.redisTemplate = redisTemplate;
        this.solapiMessageService = solapiMessageService;
        this.msgUtil = msgUtil;
    }

    @Override
    public SmsResponse send(SmsSendRequest request) {

        String phone = normalizePhone(request.phone());
        String code = generateCode();
        String key = SMS_KEY_PREFIX + phone;

        try {
            // Redis에 인증번호 저장
            redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(CODE_EXPIRATION_MINUTES));

            // SMS 발송
            Message message = new Message();
            message.setFrom(sender);
            message.setTo(phone);
            message.setText("[YSC] 인증번호는 " + code + "입니다.");

            solapiMessageService.send(message, null);

            return new SmsResponse(true,  msgUtil.getMsg("SMS", "001"));

        } catch (Exception e) {

            // 문자 발송 실패 시 Redis에 저장한 인증번호 삭제
            redisTemplate.delete(key);
            return new SmsResponse(false, msgUtil.getMsg("SMS", "005"));
        }
    }

    @Override
    public SmsResponse verify(SmsVerifyRequest request) {

        String phone = normalizePhone(request.phone());

        String key = SMS_KEY_PREFIX + phone;

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

    private String generateCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
    }

    private String normalizePhone(String phone) {
        return phone.replaceAll("[^0-9]", "");
    }
}