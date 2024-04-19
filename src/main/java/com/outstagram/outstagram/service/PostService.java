package com.outstagram.outstagram.service;

import com.outstagram.outstagram.controller.request.CreatePostReq;
import com.outstagram.outstagram.dto.PostDTO;
import com.outstagram.outstagram.mapper.PostMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;

    private final ImageService imageService;

    // parameter : 유저, 게시물 내용, 이미지들
    public void insertPost() {
        // 1. post만 먼저 insert

        // 2. insert한 post의 id 가져오기

        // 3. imageService를 통해 image insert
    }

    public void createPost(CreatePostReq createPostReq, Long userId) {
        PostDTO newPost = PostDTO.builder()
            .contents(createPostReq.getContents())
            .userId(userId)
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .build();
        postMapper.insertPost(newPost);
    }
}
