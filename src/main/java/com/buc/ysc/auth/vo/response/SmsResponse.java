package com.buc.ysc.auth.vo.response;

public record SmsResponse(
        boolean success,
        String message
) {
}