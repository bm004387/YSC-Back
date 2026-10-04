package com.buc.ysc.user.vo.reponse;

public record UserInfoResponse(
        String userId,
        String userName,
        String role
) {
}