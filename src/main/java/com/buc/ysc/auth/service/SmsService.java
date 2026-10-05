package com.buc.ysc.auth.service;

import com.buc.ysc.auth.vo.request.SmsSendRequest;
import com.buc.ysc.auth.vo.request.SmsVerifyRequest;
import com.buc.ysc.auth.vo.response.SmsResponse;

public interface SmsService {

    SmsResponse send(SmsSendRequest request);

    SmsResponse verify(SmsVerifyRequest request);
}