package com.buc.ysc.auth.vo.record;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
        String userNm,

        @NotBlank
        @Pattern(
                regexp = "^01[0-9]{8,9}$",
                message = "올바른 휴대폰 번호를 입력해주세요."
        )
        String hpNo,

        @Size(max = 255)
        String addr,

        @Size(max = 255)
        String dtlAddr

) {
}