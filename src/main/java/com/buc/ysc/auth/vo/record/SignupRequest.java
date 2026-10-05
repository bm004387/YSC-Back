package com.buc.ysc.auth.vo.record;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @NotBlank
        @Size(max = 50)
        String userId,

        @NotBlank
        @Size(min = 8, max = 72)
        String passwd,

        @NotBlank
        @Size(max = 100)
        String userName

) {
}