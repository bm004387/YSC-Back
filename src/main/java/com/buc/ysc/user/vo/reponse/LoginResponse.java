package com.buc.ysc.user.vo.reponse;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserInfoResponse user
) {
}