package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.controller.response.CommentRes;
import com.outstagram.outstagram.dto.CommentDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentMapper {

    void insertComment(CommentDTO comment);

    List<CommentRes> findByPostId(Long postId);

}
