package com.buc.ysc.auth.vo.record;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** 아이디 찾기 인증번호 발송 요청입니다. */
public record RecoveryIdSendRequest(
        @NotBlank @Pattern(regexp = "^01[0-9]{8,9}$") String hpNo
) {
}
