package com.buc.ysc.user.vo.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserVO {

    private String userId;
    private String passwd;
    private String userName;
    private String role;

    private String firstRegEmpNo;
    private String firstRegDtm;

    private String lastModEmpNo;
    private String lastModDtm;


}