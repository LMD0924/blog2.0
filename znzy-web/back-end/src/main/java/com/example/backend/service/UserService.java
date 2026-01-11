package com.example.backend.service;

import com.example.backend.entity.User;

import java.util.Date;
import java.util.List;


public interface UserService {
    User login(String username, String password);
    User getUserById(Integer id);
    boolean register(User user);
    //获取所有用户
    List<User> getAllUser(Integer id);
    //管理员审核用户
    int updateUserStatus(Integer id,String status,String role);
    boolean updateUserAvatar(Integer userId, String avatarUrl);
    //关注他人
    int InsertFollow(Integer userId, Integer followId, Date time);
    //取消关注他人
    int DeleteFollow(Integer userId,Integer followId);
    //检查是否关注
    Integer CheckFollow(Integer userId,Integer followId);
    //修改信息
    int updateUserInfo(User user);
}
