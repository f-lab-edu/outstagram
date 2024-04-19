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

    public void insertPost(CreatePostReq createPostReq, Long userId) {
        PostDTO newPost = PostDTO.builder()
            .contents(createPostReq.getContents())
            .userId(userId)
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .build();

        // 게시물 내용 저장 (insertPost 정상 실행되면, newPost의 id 속성에 id값이 들어 있다)
        postMapper.insertPost(newPost);

        // 로컬 디렉토리에 이미지 저장 후, DB에 이미지 정보 저장
        imageService.saveImages(createPostReq.getImgFiles(),
            newPost.getId());

    }
}
