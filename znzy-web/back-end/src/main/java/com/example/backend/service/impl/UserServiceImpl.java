package com.example.backend.service.impl;

import com.example.backend.entity.User;
import com.example.backend.mapper.FollowMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    UserMapper userMapper;
    @Resource
    FollowMapper followMapper;

    @Override
    public User login(String username, String password) {
        return userMapper.login(username, password);
    }

    @Override
    public User getUserById(Integer id) {
        return userMapper.getUserById(id);
    }

    //获取所有用户
    @Override
    public List<User> getAllUser(Integer id){
        //管理员可以查看所有用户
        if(id==1) return userMapper.getAllUser();
        else return null;
    }
    //管理员审核用户状态
    @Override
    public int updateUserStatus(Integer id,String status,String role){
        return userMapper.updateUserStatus(id,status,role);
    }
    @Override
    public boolean register(User user) {
        // 检查用户名是否已存在
        if (userMapper.findByName(user.getName()) != null) {
            return false;
        }
        // 检查邮箱是否已存在
        if (userMapper.findByEmail(user.getEmail()) != null) {
            return false;
        }
        // 插入新用户
        userMapper.insertUser(user);
        return true;
    }
    @Override
    public boolean updateUserAvatar(Integer userId, String avatarUrl) {
        try {
            userMapper.updateUserAvatar(userId, avatarUrl);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    //检查是否关注
    @Override
    public Integer CheckFollow(Integer userId,Integer followId){
        int count = followMapper.CheckFollow(userId,followId);
        return count > 0 ? 1 : 0;
    }
    //关注
    @Override
    @Transactional
    public int InsertFollow(Integer userId, Integer followId, Date time){
        try{
            //检查是否关注
            int CheckFollow=followMapper.CheckFollow(userId,followId);
            if(CheckFollow==1) throw new RuntimeException("您已关注该用户");
            //没有关注的话计修改follow和user表里的数据
            int addFollow=userMapper.addFollow(userId);
            if(addFollow==0) throw new RuntimeException("更新user表的follow失败");
            int addFans=userMapper.addFans(userId);
            if(addFans==0) throw new RuntimeException("更新user表的fans失败");
            int InsertFollow=followMapper.InsertFollow(userId,followId,time);
            if(InsertFollow==0) throw new RuntimeException("更新follow表失败");
            return 1;
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
    //取消关注
    @Override
    @Transactional
    public int DeleteFollow(Integer userId,Integer followId){
        try{
            //检查是否关注
            int status=CheckFollow(userId,followId);
            if(status==0) throw new RuntimeException("您未关注该用户");
            //已经关注的话计修改follow和user表里的数据
            int subFollow=userMapper.subFollow(userId);
            if(subFollow==0) throw new RuntimeException("更新user表的follow失败");
            int subFans=userMapper.subFans(userId);
            if(subFans==0) throw new RuntimeException("更新user表的fans失败");
            int DeleteFollow=followMapper.DeleteFollow(userId,followId);
            if(DeleteFollow==0) throw new RuntimeException("更新follow表失败");
            return 1;
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
    //修改信息
    @Override
    public int updateUserInfo(User user){
        return userMapper.updateUserInfo(user);
    }
}
