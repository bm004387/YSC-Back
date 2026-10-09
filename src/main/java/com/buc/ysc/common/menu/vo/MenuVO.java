package com.buc.ysc.common.menu.vo;

import lombok.Data;

@Data
public class MenuVO {
    private String menuId;
    private String menuName;
    private String upperMenuId;
    private Integer menuLevel;
    private Integer sortOrder;
    private String menuType;
    private String iconName;
    private String programCode;
    private String programUrl;
}
