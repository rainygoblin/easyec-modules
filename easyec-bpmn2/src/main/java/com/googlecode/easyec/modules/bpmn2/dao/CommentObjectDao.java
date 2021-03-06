package com.googlecode.easyec.modules.bpmn2.dao;

import com.googlecode.easyec.modules.bpmn2.domain.CommentObject;

import java.util.List;

/**
 * Created by 俊杰 on 2014/7/10.
 */
public interface CommentObjectDao {

    List<CommentObject> selectAllComments(String processInstanceId);

    List<CommentObject> selectApprovedCommentsByTaskId(String taskId);

    List<CommentObject> selectAnnotatedCommentsByTaskId(String taskId);

    int insertExtraInfo(CommentObject comment);
}
