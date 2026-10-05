package com.buc.ysc.common.msg.mapper;

import com.buc.ysc.common.msg.vo.MsgVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MsgMapper {

    List<MsgVO> getMsgList();
}