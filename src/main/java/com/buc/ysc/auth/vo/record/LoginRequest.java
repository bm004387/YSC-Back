package com.buc.ysc.auth.vo.record;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank
        String usrId,

        @NotBlank
        String pwd,

        boolean rememberMe

) {
}
