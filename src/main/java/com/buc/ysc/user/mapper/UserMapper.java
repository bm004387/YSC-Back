package com.buc.ysc.user.mapper;

import com.buc.ysc.user.vo.request.UserVO;

public interface UserMapper {

    UserVO selectByUsrId(String usrId);

    int existsByUsrId(String usrId);

    boolean existsHpNo(String hpNo);


    void insertUser(UserVO usrId);
}