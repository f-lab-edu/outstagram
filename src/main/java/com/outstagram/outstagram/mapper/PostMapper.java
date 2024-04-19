package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.dto.PostDTO;
import org.springframework.stereotype.Repository;

@Repository
public interface PostMapper {

    int insertPost(PostDTO post);
}
