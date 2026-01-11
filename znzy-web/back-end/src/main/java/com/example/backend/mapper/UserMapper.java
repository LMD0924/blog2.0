package com.example.backend.mapper;

import com.example.backend.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE name=#{name} AND password=#{password}")
    User login(String name, String password);

    @Select("SELECT * FROM user WHERE id=#{id}")
    User getUserById(Integer id);

    @Insert("INSERT INTO user (name, password, avatar, email,account,role) VALUES (#{name}, #{password}, #{avatar}, #{email},#{account},#{role})")
    void insertUser(User user);

    @Select("SELECT * FROM user WHERE name = #{name}")
    User findByName(String name);

    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);

    //获取所有用户
    @Select("select * from user")
    List<User> getAllUser();
    //管理员审核用户
    @Update("update user set status=#{status},role=#{role} where id=#{id}")
    int updateUserStatus(Integer id,String status,String role);
    @Update("UPDATE user SET avatar = #{avatarUrl} WHERE id = #{userId}")
    void updateUserAvatar(@Param("userId") Integer userId, @Param("avatarUrl") String avatarUrl);
    //关注他人
    @Update("update user set follow=coalesce(follow,0)+1 where id=#{id}")
    int addFollow(Integer id);
    //取消关注他人
    @Update("update user set follow=coalesce(follow,0)-1 where id=#{id}")
    int subFollow(Integer id);
    //关注我
    @Update("update user set fans=coalesce(fans,0)+1 where id=#{id}")
    int addFans(Integer id);
    //取消关注我
    @Update("update user set fans=coalesce(fans,0)-1 where id=#{id}")
    int subFans(Integer id);
    //修改个人信息
    @Update("update user set name=#{name},email=#{email},account=#{account} where id=#{id}")
    int updateUserInfo(User user);
}
