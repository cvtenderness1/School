package com.project.platform.service.impl;

import com.project.platform.entity.Comment;
import com.project.platform.mapper.CommentMapper;
import com.project.platform.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Override
    public boolean submitComment(Comment comment) {
        comment.setCreateTime(java.time.LocalDateTime.now());
        return commentMapper.insertComment(comment) > 0;
    }

    @Override
    public Comment getByOrderId(String orderId) {
        return commentMapper.selectByOrderId(orderId);
    }

    @Override
    public boolean isCommented(String orderId) {
        return commentMapper.countByOrderId(orderId) > 0;
    }
    @Override
    public List<Comment> getByMerchantId(Integer merchantId) {
        return commentMapper.selectByMerchantId(merchantId);
    }
}