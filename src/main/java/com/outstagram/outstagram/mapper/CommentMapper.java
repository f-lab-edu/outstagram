package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.dto.CommentDTO;
import com.outstagram.outstagram.dto.CommentUserDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentMapper {

    void insertComment(CommentDTO comment);

    List<CommentUserDTO> findByPostId(Long postId);

    CommentDTO findById(Long postId, Long commentId);

    int updateContentsById(Long postId, Long commentId, String contents);

    int deleteComment(Long postId, Long commentId);

    int deleteByPostId(Long postId);
}
