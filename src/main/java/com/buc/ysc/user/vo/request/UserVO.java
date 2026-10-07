package com.buc.ysc.user.vo.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserVO {

    private String usrId;          /* 사용자아이디 */
    private String pwd;          /* 비밀번호 */
    private String usrNm;          /* 사용자명 */
    private String rol;            /* 권한 */
    private String hpNo;            /* 핸드폰번호 */
    private String adr;            /* 주소 */
    private String dtlAdr;         /* 상세주소 */
    private String joinDtm;       /* 최초가입일시 */

}