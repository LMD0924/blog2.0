package com.example.backend.controller;

import com.example.backend.entity.RestBean;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import com.example.backend.util.FileUploadUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jdk.swing.interop.SwingInterOpUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    UserService userService;
    @Resource
    FileUploadUtil fileUploadUtil;
    @GetMapping("information")
    public RestBean<User> getMyInfo(HttpServletRequest request){//没有正确的令牌的话进不去，所以不用判断是不是空了
//        if(session.getAttribute("user")==null)return RestBean.failure(401,"未登陆~");
        User theUser=userService.getUserById((Integer) request.getAttribute("id"));
        theUser.setPassword(null);
        //获取键为user的值
        return RestBean.success("获取成功！",theUser);
    }

    @GetMapping("getUserById")
    public RestBean<User> getUserById(@RequestParam("id") Integer id){
        User theUser=userService.getUserById(id);
        theUser.setPassword(null);
        return RestBean.success("获取成功！",theUser);
    }

    @PostMapping("register")
    public RestBean<String> register(
            @RequestParam("name") String name,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam("account") String account,
            @RequestParam(value = "avatar", required = false) String avatar) { // avatar 是可选的
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        user.setEmail(email);
        user.setAccount(account);
        user.setAvatar(avatar); // 设置 avatar

        if (userService.register(user)) {
            return RestBean.success("注册成功！");
        } else {
            return RestBean.failure(400, "注册失败：用户名或邮箱已存在！");
        }
    }
    //获取个人信息
    @GetMapping("current")
    public RestBean<User> getCurrentUser(HttpServletRequest request) {
        int userId = (Integer) request.getAttribute("id"); // 从请求中获取用户 ID
        User user = userService.getUserById(userId); // 查询用户信息
        user.setPassword("***不给看略略略***");
        return RestBean.success("获取成功！", user);
    }
    //获取所有用户
    @GetMapping("getAllUser")
    public RestBean<List<User>> getAllUser(@RequestParam("id") Integer id,
                                           HttpServletRequest request){
        int userId=(Integer) request.getAttribute("id");
        if(userId!=1){
            return RestBean.failure(400,"权限不足");
        }
        List<User> users=userService.getAllUser(id);
        for(User user:users){
            user.setPassword(null);
        }
        return RestBean.success("获取成功！",users);
    }
    //管理员审核用户状态
    @PostMapping("updateUserStatus")
    public RestBean<Integer> updateUserStatus(@RequestParam("id") Integer id,
                                              @RequestParam("status") String status,
                                              @RequestParam("role") String role,
                                              HttpServletRequest request){
        System.out.print("前端传入的状态:"+status);
        int userId=(Integer) request.getAttribute("id");
        int cs=userService.updateUserStatus(id,status,role);
        if(userId!=1){
            return RestBean.failure(400,"权限不足!");
        }else{
             return RestBean.success("审核成功！！！",cs);
        }
    }
//上传头像
    @PostMapping("/upload-avatar")
    public RestBean<String> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            // 验证文件类型
            if (!fileUploadUtil.isImageFile(file)) {
                return RestBean.failure(400, "只能上传图片文件");
            }

            // 验证文件大小
            if (file.getSize() > 5 * 1024 * 1024) { // 5MB限制
                return RestBean.failure(400, "图片大小不能超过5MB");
            }

            // 上传文件并获取URL
            String avatarUrl = fileUploadUtil.uploadFile(file);

            // 更新用户头像
            Integer userId = (Integer) request.getAttribute("id");
            User user = userService.getUserById(userId);
            user.setAvatar(avatarUrl);
            userService.updateUserAvatar(userId, avatarUrl);

            return RestBean.success("头像上传成功", avatarUrl);

        } catch (IOException e) {
            return RestBean.failure(500, "头像上传失败: " + e.getMessage());
        }
            }
      //关注
    @PostMapping("/follow")
    @Transactional
    public RestBean<Integer> addFollow(@RequestParam("followId") Integer followId,
                                       HttpServletRequest request){
        int userId=(Integer) request.getAttribute("id");
        int status=userService.InsertFollow(userId,followId,new Date());
        if(status==1) return RestBean.success("关注成功",status);
        else return RestBean.failure(400,"关注失败");
    }
    //取消关注
    @PostMapping("/unfollow")
    @Transactional
    public RestBean<Integer> subFollow(@RequestParam("followId") Integer followId,
                                       HttpServletRequest request){
        int userId=(Integer) request.getAttribute("id");
        int status=userService.DeleteFollow(userId,followId);
        if(status==1) return RestBean.success("取消关注成功",status);
        else return RestBean.failure(400,"取消关注失败");
    }
    //检查是否关注
    @GetMapping("checkFollow")
    public RestBean<Integer> checkFollow(@RequestParam("followId") Integer followId,
                                         HttpServletRequest request){
        int userId=(Integer) request.getAttribute("id");
        int status=userService.CheckFollow(userId,followId);
        if(status==1) return RestBean.success("已关注",status);
        else return RestBean.failure(400,"你还未关注该用户");
    }
    //修改信息
    @PostMapping("updateUser")
    public RestBean<Integer> updateUser(User user,
                                        HttpServletRequest request){
        int userId=(int) request.getAttribute("id");
        if(userId==user.getId()){
            int status=userService.updateUserInfo(user);
            if(status==1) return RestBean.success("修改成功",status);
            else return RestBean.failure(400,"修改失败");
        }else{
            return RestBean.failure(400,"权限不足");
        }
    }
}
