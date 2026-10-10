package com.buc.ysc.user.mapper;

import com.buc.ysc.user.vo.request.UserVO;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    UserVO selectByUsrId(String usrId);

    int existsByUsrId(String usrId);

    boolean existsHpNo(String hpNo);


    void insertUser(UserVO usrId);

    int updatePassword(UserVO user);

    int updatePinPassword(UserVO user);

    int updateAddress(UserVO user);

    int updateProfileImageFileSeq(UserVO user);

}
