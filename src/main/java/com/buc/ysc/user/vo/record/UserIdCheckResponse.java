package com.buc.ysc.user.vo.record;

public record UserIdCheckResponse(
        boolean available,
        String message
) {
}