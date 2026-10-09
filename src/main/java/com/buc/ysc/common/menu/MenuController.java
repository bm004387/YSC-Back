package com.buc.ysc.common.menu;

import com.buc.ysc.common.menu.mapper.MenuMapper;
import com.buc.ysc.common.menu.vo.MenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuMapper menuMapper;

    @GetMapping(value = "/bottom", produces = "application/json; charset=UTF-8")
    public List<MenuVO> getBottomMenuList() {
        return menuMapper.getBottomMenuList();
    }

    @GetMapping(value = "/all", produces = "application/json; charset=UTF-8")
    public List<MenuVO> getAllMenuList() {
        return menuMapper.getAllMenuList();
    }
}
