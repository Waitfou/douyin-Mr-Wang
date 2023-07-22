package com.wangguo.service.impl;

import com.wangguo.bo.UpdatedUserBO;
import com.wangguo.enums.Sex;
import com.wangguo.enums.UserInfoModifyType;
import com.wangguo.enums.YesOrNo;
import com.wangguo.exceptions.GraceException;
import com.wangguo.grace.result.ResponseStatusEnum;
import com.wangguo.mapper.UsersMapper;
import com.wangguo.pojo.Users;
import com.wangguo.service.UserService;
import com.wangguo.utils.DateUtil;
import com.wangguo.utils.DesensitizationUtil;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
// Tkmybatis 是基于 Mybatis 框架开发的一个工具，通过调用它提供的方法实现对单表的数据操作
// （相比于Mybatis-Spring默认操作更加丰富），不需要写任何 sql 语句，这极大地提高了项目开发效率。
/**
 * tkmybatis 具体的使用是在 service 层，service 层又分为接口和接口实现类，具体就在接口实现类里面。
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private Sid sid;
    private static final String USER_FACE1 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxF6ZUySASMbOAABBAXhjY0Y649.png";

    /**
     * 判断用户是否存在
     * @param mobile 手机号
     * @return 返回查询到饿用户信息
     */
    @Override
    public Users queryMobileIsExist(String mobile) {
        // mybatis的逆向工程中会生成实例及实例对应的example，example用于添加条件，相当where后面的部分
        // Example 对象包含了我们各种自定义的查询条件，相当于 sql 语句中 where 部分的条件。
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("mobile", mobile);
        Users user = usersMapper.selectOneByExample(userExample);
        return user;
    }

    /**
     * 注册用户
     * @param mobile 手机号
     * @return 注册到的用户信息对象
     */
//    @Transactional 注解应该只被应用到 public 方法上，这是由 Spring AOP 的本质决定的。
    @Transactional //实质是使用了 JDBC 的事务来进行事务控制的, 事务要遵循ACID原则
    @Override
    public Users createUser(String mobile) {
        String userId = sid.nextShort(); // 获取全局的唯一主键
        Users user = new Users();
        user.setId(userId);
        user.setMobile(mobile);
        user.setNickname("用户：" + DesensitizationUtil.commonDisplay(mobile));
        user.setIdouyinNum("用户：" + DesensitizationUtil.commonDisplay(mobile));
        user.setFace(USER_FACE1);
        user.setBirthday(DateUtil.stringToDate("1900-01-01"));
        user.setSex(Sex.secret.type);

        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");
        user.setDescription("这家伙很懒。什么都没留下~");
        user.setCanIdouyinNumBeUpdated(YesOrNo.YES.type);

        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());

        usersMapper.insert(user); // 要往数据表中插入数据了，需要保证事务的ACID特性。
        return user;
    }

    /**
     * 根据用户id查询用户的信息
     * @param userId 用户Id
     * @return 返回匹配的用户实体信息
     */
    @Override
    public Users getUser(String userId) {
        return usersMapper.selectByPrimaryKey(userId);
    }

    /**
     * 更新用户信息
     * @param updatedUserBO 更新的用户信息集合
     * @return 更改之后的用户
     */
    @Transactional
    @Override
    public Users updateUsersInfo(UpdatedUserBO updatedUserBO) {
        // pending表示等待被处理
        Users pendingUser = new Users();
        // copyProperties是浅拷贝
        BeanUtils.copyProperties(updatedUserBO, pendingUser); //把updatedUserBO中的属性值拷贝给pendingUser

        int result = usersMapper.updateByPrimaryKeySelective(pendingUser);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
        }
        return getUser(updatedUserBO.getId());
    }

    /**
     * 修改用户指定类型的信息
     * @param updatedUserBO 要更新的用户的相关信息集合
     * @param type 要修改的信息类型
     * @return
     */
    @Transactional
    @Override
    public Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type) {
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        // 如果要修改的内容是昵称，不能重复
        if (type == UserInfoModifyType.NICKNAME.type) { //查询这个昵称有没有用户使用
            criteria.andEqualTo("nickname", updatedUserBO.getNickname());
            Users user = usersMapper.selectOneByExample(example);
            if (user != null) {
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
            }
        }
        // 如果要修改的是idouyin号，那么也不能重复。除了昵称和idouyin号其他都可以重复
        if (type == UserInfoModifyType.IDOUYINNUM.type) {
            criteria.andEqualTo("idouyinNum", updatedUserBO.getIdouyinNum());
            Users user = usersMapper.selectOneByExample(example);
            if (user != null) {
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
            }
            Users tempUser = getUser(updatedUserBO.getId());
            // 如果IDOUYIN账号不能修改
            if (tempUser.getCanIdouyinNumBeUpdated() == YesOrNo.NO.type) {
                GraceException.display(ResponseStatusEnum.USER_INFO_CANT_UPDATED_IDOUYINNUM_ERROR);
            }
            // 保证用户只能修改一次idouyin号
            updatedUserBO.setCanIdouyinNumBeUpdated(YesOrNo.NO.type);
        }
        return updateUsersInfo(updatedUserBO);
    }
}
