package com.wangguo.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data //注解在类上, 为类提供读写属性, 此外还提供了 equals()、hashCode()、toString() 方法
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdatedUserBO {
    private String id;
    private String nickname;
    private String idouyinNum;
    private String face;
    private Integer sex;
    private Date birthday;
    private String country;
    private String province;
    private String city;
    private String district;
    private String description;
    private String bgImg;
    private Integer canIdouyinNumBeUpdated;
}
