package com.buc.ysc.auth.vo.record;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank
        String userId,

        @NotBlank
        String passwd

) {
}