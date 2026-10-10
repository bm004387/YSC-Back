package com.buc.ysc.common.menu.mapper;

import com.buc.ysc.common.menu.vo.MenuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper {
    List<MenuVO> getBottomMenuList(@Param("menuType") String menuType);
    List<MenuVO> getAllMenuList();
}
