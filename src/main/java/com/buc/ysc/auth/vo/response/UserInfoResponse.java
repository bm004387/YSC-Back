package com.buc.ysc.auth.vo.response;

public record UserInfoResponse(
        String usrId,
        String usrNm,
        String rol,
        String hpNo,
        String adr,
        String dtlAdr
) {
}
