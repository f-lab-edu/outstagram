package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.controller.response.CommentRes;
import com.outstagram.outstagram.dto.CommentDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentMapper {

    void insertComment(CommentDTO comment);

    List<CommentRes> findByPostId(Long postId);

    CommentDTO findById(Long commentId);

    int updateContentsById(Long commentId, String contents);

    int deleteComment(Long commentId);
}
