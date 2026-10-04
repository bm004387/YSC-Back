package com.buc.ysc.user.mapper;

import com.buc.ysc.user.vo.request.UserVO;

public interface UserMapper {

    UserVO selectByUserId(String userId);

    int existsByUserId(String userId);

    void insertUser(UserVO userId);
}