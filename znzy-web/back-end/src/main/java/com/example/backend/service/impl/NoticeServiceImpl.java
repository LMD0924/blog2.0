package com.example.backend.service.impl;

import com.example.backend.entity.Notice;
import com.example.backend.mapper.NoticeMapper;
import com.example.backend.service.NoticeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * @Author:总会落叶
 * @Date:2025/10/28
 * @Description:
 */
@Service
public class NoticeServiceImpl implements NoticeService {
    @Resource
    NoticeMapper noticeMapper;
    //发布消息
    @Override
    public void InsertNotice(Notice notice){
        noticeMapper.InsertNotice(notice);
    }
    //获取自己发布的消息
    @Override
    public List<Notice> SelectNoticeById(Integer id){
        return noticeMapper.SelectNoticeById(id);
    }
    //获取所有消息
    @Override
    public List<Notice> SelectAllNotice(){
        return noticeMapper.SelectAllNotice();
    }
    //修改消息
    @Override
    public void UpdateNotice(Notice notice){
        noticeMapper.UpdateNotice(notice);
    }
}
