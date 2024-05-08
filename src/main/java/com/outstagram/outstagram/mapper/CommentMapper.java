package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.dto.CommentDTO;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentMapper {

    void insertComment(CommentDTO comment);

}
