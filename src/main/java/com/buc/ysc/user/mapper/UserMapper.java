package com.buc.ysc.user.mapper;

import com.buc.ysc.user.vo.request.UserVO;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    UserVO selectByUsrId(String usrId);

    int existsByUsrId(String usrId);

    boolean existsHpNo(String hpNo);


    void insertUser(UserVO usrId);

    int updatePassword(@Param("usrId") String usrId, @Param("pwd") String pwd);

    int updateAddress(@Param("usrId") String usrId, @Param("adr") String adr, @Param("dtlAdr") String dtlAdr);

    int updateProfileImageFileSeq(@Param("usrId") String usrId, @Param("filSeq") Long filSeq);

}
