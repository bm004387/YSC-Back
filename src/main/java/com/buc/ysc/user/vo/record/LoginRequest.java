package com.buc.ysc.user.vo.record;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank
        String userId,

        @NotBlank
        String passwd

) {
}