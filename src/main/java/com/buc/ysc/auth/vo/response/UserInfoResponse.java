package com.buc.ysc.auth.vo.response;

public record UserInfoResponse(
        String userId,
        String userNm,
        String role
) {
}