package com.buc.ysc.auth.vo.record;

public record UserIdCheckResponse(
        boolean available,
        String message
) {
}