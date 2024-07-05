package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.CommentDTO;
import com.outstagram.outstagram.dto.CommentUserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.CommentMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;

    public void insertComment(CommentDTO comment) {
        commentMapper.insertComment(comment);
    }

    public List<CommentUserDTO> getComments(Long postId) {
        return commentMapper.findByPostId(postId);
    }

    public CommentDTO findByIdAndPostId(Long postId, Long commentId) {
        return commentMapper.findByIdAndPostID(postId, commentId);
    }

    public CommentDTO findById(Long commentId) {
        return commentMapper.findById(commentId);
    }

    public void updateContents(Long postId, Long commentId, String contents) {
        int result = commentMapper.updateContentsById(postId, commentId, contents);
        if (result == 0) {
            throw new ApiException(ErrorCode.UPDATE_ERROR);
        }
    }

    public void deleteComment(Long postId, Long commentId) {
        int result = commentMapper.deleteComment(postId, commentId);
        if (result == 0) {
            throw new ApiException(ErrorCode.DELETE_ERROR);
        }
    }
}
