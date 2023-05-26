package com.wangguo.service;

import com.wangguo.bo.UpdatedUserBO;
import com.wangguo.pojo.Users;

public interface UserService {
    /**
     * 查询手机号是否存在
     * @param mobile
     * @return
     */
    public Users queryMobileIsExist(String mobile);

    /**
     * 创建用户
     * @param mobile
     * @return
     */
    public Users createUser(String mobile);

    /**
     * 获取用户信息
     * @param userId
     * @return
     */
    public Users getUser(String userId);

    /**
     * 更新用户信息
     * @param updatedUserBO
     * @return
     */
    public Users updateUsersInfo(UpdatedUserBO updatedUserBO);

    /**
     * 更新用户信息
     * @param updatedUserBO
     * @param type
     * @return
     */
    public Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type);
}
