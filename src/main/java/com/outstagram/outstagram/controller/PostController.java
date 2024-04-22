package com.outstagram.outstagram.controller;

import com.outstagram.outstagram.common.annotation.Login;
import com.outstagram.outstagram.common.api.ApiResponse;
import com.outstagram.outstagram.controller.request.PostCreateReq;
import com.outstagram.outstagram.controller.request.PostEditReq;
import com.outstagram.outstagram.controller.response.PostRes;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse> createPost(@ModelAttribute @Valid PostCreateReq postCreateReq, @Login UserDTO user) {
        postService.insertPost(postCreateReq, user.getId());

        return ResponseEntity.ok(ApiResponse.builder().isSuccess(true).httpStatus(HttpStatus.OK).message("게시물을 저장했습니다.").build());
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostRes> getPost(@PathVariable Long postId, @Login UserDTO user) {
        PostRes postRes = postService.getPost(postId, user.getId());

        return ResponseEntity.ok(postRes);
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse> editPost(@PathVariable Long postId, @ModelAttribute @Valid PostEditReq postEditReq, @Login UserDTO user) {
        postService.editPost(postId, postEditReq, user.getId());

        return ResponseEntity.ok(ApiResponse.builder().isSuccess(true).httpStatus(HttpStatus.OK).message("게시물 수정 완료했습니다.").build());
    }
}


