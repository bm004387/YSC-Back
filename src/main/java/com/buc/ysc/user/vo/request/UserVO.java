package com.buc.ysc.user.vo.request;

public class UserVO {

    private String userId;
    private String passwd;
    private String userName;
    private String role;

    private String firstRegEmpNo;
    private String firstRegDtm;

    private String lastModEmpNo;
    private String lastModDtm;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


    public String getFirstRegEmpNo() {
        return firstRegEmpNo;
    }

    public void setFirstRegEmpNo(String firstRegEmpNo) {
        this.firstRegEmpNo = firstRegEmpNo;
    }


    public String getFirstRegDtm() {
        return firstRegDtm;
    }

    public void setFirstRegDtm(String firstRegDtm) {
        this.firstRegDtm = firstRegDtm;
    }


    public String getLastModEmpNo() {
        return lastModEmpNo;
    }

    public void setLastModEmpNo(String lastModEmpNo) {
        this.lastModEmpNo = lastModEmpNo;
    }


    public String getLastModDtm() {
        return lastModDtm;
    }

    public void setLastModDtm(String lastModDtm) {
        this.lastModDtm = lastModDtm;
    }
}