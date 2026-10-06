package com.buc.ysc.user.vo.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserVO {

    private String userId;          /* 사용자아이디 */
    private String passwd;          /* 비밀번호 */
    private String userNm;          /* 사용자명 */
    private String role;            /* 권한 */
    private String hpNo;            /* 핸드폰번호 */
    private String addr;            /* 주소 */
    private String dtlAddr;         /* 상세주소 */
    private String createdAt;       /* 최초가입일시 */

}