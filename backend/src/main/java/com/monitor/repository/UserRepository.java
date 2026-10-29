package com.monitor.repository;

import org.apache.ibatis.annotations.Mapper;

import com.monitor.entity.User;
import com.monitor.entity.table.UserTableDef;
import com.mybatisflex.core.BaseMapper;

/**
 * 用户Repository接口
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    default User findByUsername(String username) {
        return selectOneByCondition(
            UserTableDef.USER.USERNAME.eq(username)
                .and(UserTableDef.USER.DELETED.eq(0))
        );
    }
}
