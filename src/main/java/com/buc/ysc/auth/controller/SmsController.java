package com.buc.ysc.auth.controller;

import com.buc.ysc.auth.service.SmsService;
import com.buc.ysc.auth.vo.request.SmsSendRequest;
import com.buc.ysc.auth.vo.request.SmsVerifyRequest;
import com.buc.ysc.auth.vo.response.SmsResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    /**
     * SMS 인증번호 발송
     */
    @PostMapping("/send")
    public ResponseEntity<SmsResponse> send(@Valid @RequestBody SmsSendRequest request) {
        return ResponseEntity.ok(smsService.send(request));
    }

    /**
     * SMS 인증번호 확인
     */
    @PostMapping("/verify")
    public ResponseEntity<SmsResponse> verify(@Valid @RequestBody SmsVerifyRequest request) {

        return ResponseEntity.ok(smsService.verify(request));
    }
}