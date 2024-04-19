package com.outstagram.outstagram.controller;

import com.outstagram.outstagram.common.annotation.Login;
import com.outstagram.outstagram.common.api.ApiResponse;
import com.outstagram.outstagram.controller.request.CreatePostReq;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse> createPost(@ModelAttribute CreatePostReq createPostReq,
        @Login UserDTO user) {
        postService.insertPost(createPostReq, user.getId());

        return ResponseEntity.ok(
            ApiResponse.builder().isSuccess(true).httpStatus(HttpStatus.OK).message("게시물을 저장했습니다.")
                .build());
    }
}


