package com.wangguo.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * BO的意思是Business Object 业务对象，但是要区别于直接对外提供服务的服务层
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RegistLoginBO {
    /**
     * hibernate的校验工具
     */
    @NotBlank(message = "手机号不能为空")  //和@Valid搭配使用
    @Length(min = 11, max = 11, message = "手机长度不正确")
    private String mobile;
    @NotBlank(message = "验证码不能为空")
    private String smsCode;
}
