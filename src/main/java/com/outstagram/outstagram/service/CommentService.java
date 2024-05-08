package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.CommentDTO;
import com.outstagram.outstagram.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;

    public void insertComment(CommentDTO comment) {
        commentMapper.insertComment(comment);
    }

}
