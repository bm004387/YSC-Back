package com.buc.ysc.common.msg.mapper;

import com.buc.ysc.common.msg.vo.MsgVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MsgMapper {

    String getMsg(String menuId, String msgCd);

    List<MsgVO> getMsgList(String menuId);
}