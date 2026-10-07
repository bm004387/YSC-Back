package com.buc.ysc.auth.vo.record;

public record UsrIdCheckResponse(
        boolean available,
        String message
) {
}