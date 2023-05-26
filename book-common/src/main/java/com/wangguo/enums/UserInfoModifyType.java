package com.wangguo.enums;

import com.wangguo.exceptions.GraceException;
import com.wangguo.grace.result.ResponseStatusEnum;

public enum UserInfoModifyType {
    NICKNAME(1, "昵称"),
    IDOUYINNUM(2, "慕课号"),
    SEX(3, "性别"),
    BIRTHDAT(4, "生日"),
    LOCATION(5, "所在地"),
    DESC(6, "简介");
    public final Integer type;
    public final String value;

    UserInfoModifyType(Integer type, String value) {
        this.type = type;
        this.value = value;
    }

    public static void checkUserInfoTypeIsRight(Integer type) {
        if (type != UserInfoModifyType.NICKNAME.type &&
            type != UserInfoModifyType.IDOUYINNUM.type &&
            type != UserInfoModifyType.SEX.type &&
            type != UserInfoModifyType.BIRTHDAT.type &&
            type != UserInfoModifyType.LOCATION.type &&
            type != UserInfoModifyType.DESC.type) {
            GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_ERROR);
        }
    }
}
