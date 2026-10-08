package com.buc.ysc.user.vo.response;

public record UserProfileResponse(
        String usrId,
        String usrNm,
        String hpNo,
        String adr,
        String dtlAdr,
        String profileImageUrl
) {
}
