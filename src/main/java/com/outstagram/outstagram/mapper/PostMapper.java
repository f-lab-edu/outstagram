package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.dto.PostDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface PostMapper {

    int insertPost(PostDTO post);

    PostDTO findById(Long postId);

    int updateContentsById(Long postId, String contents);

    int deleteById(Long postId);

    List<PostDTO> findByUserId(Long userId);
}
