package com.project.platform.service;

import com.project.platform.entity.Comment;

import java.util.List;

public interface CommentService {
    boolean submitComment(Comment comment);
    Comment getByOrderId(String orderId);
    boolean isCommented(String orderId);

    List<Comment> getByMerchantId(Integer merchantId);
}