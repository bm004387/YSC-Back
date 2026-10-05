package com.buc.ysc.auth.vo.response;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserInfoResponse user
) {
}