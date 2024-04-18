package com.outstagram.outstagram.service;

import com.outstagram.outstagram.mapper.PostMapper;
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

}
