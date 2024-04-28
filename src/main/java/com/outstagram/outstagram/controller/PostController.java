package com.outstagram.outstagram.controller;

import com.outstagram.outstagram.common.annotation.Login;
import com.outstagram.outstagram.common.api.ApiResponse;
import com.outstagram.outstagram.controller.request.CreatePostReq;
import com.outstagram.outstagram.controller.request.EditPostReq;
import com.outstagram.outstagram.controller.response.MyPostsRes;
import com.outstagram.outstagram.controller.response.PostRes;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
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
    public ResponseEntity<ApiResponse> createPost(
        @ModelAttribute @Valid CreatePostReq createPostReq, @Login UserDTO user) {
        postService.insertPost(createPostReq, user.getId());

        return ResponseEntity.ok(
            ApiResponse.builder().isSuccess(true).httpStatus(HttpStatus.OK).message("게시물을 저장했습니다.")
                .build());
    }

    @GetMapping
    public ResponseEntity<List<MyPostsRes>> getMyPosts(@RequestParam(required = false) Long lastId, @Login UserDTO user) {
        // 첫 요청일 때는 lastId는 null로 옴 -> Long의 최댓값 대입
        Long lastID = (lastId == null) ? Long.MAX_VALUE : lastId;
        List<MyPostsRes> myPosts = postService.getMyPosts(user.getId(), lastID);

        return ResponseEntity.ok(myPosts);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostRes> getPost(@PathVariable Long postId, @Login UserDTO user) {
        PostRes postRes = postService.getPost(postId, user.getId());

        return ResponseEntity.ok(postRes);
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse> editPost(@PathVariable Long postId,
        @ModelAttribute @Valid EditPostReq editPostReq, @Login UserDTO user) {
        postService.editPost(postId, editPostReq, user.getId());

        return ResponseEntity.ok(ApiResponse.builder().isSuccess(true).httpStatus(HttpStatus.OK)
            .message("게시물 수정 완료했습니다.").build());
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse> deletePost(@PathVariable Long postId, @Login UserDTO user) {
        postService.deletePost(postId, user.getId());

        return ResponseEntity.ok(ApiResponse.builder().isSuccess(true).httpStatus(HttpStatus.OK)
            .message("게시물 삭제 완료했습니다.").build());
    }

    @GetMapping("/likes")
    public ResponseEntity<List<MyPostsRes>> getLikePosts(@Login UserDTO user) {
        List<MyPostsRes> myLikePosts =  postService.getLikePosts(user.getId());

        return ResponseEntity.ok(myLikePosts);
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse> addLike(@PathVariable Long postId, @Login UserDTO user) {
        postService.increaseLike(postId, user.getId());

        return ResponseEntity.ok(ApiResponse.builder().isSuccess(true).httpStatus(HttpStatus.OK)
            .message("게시물 좋아요가 성공했습니다.").build());
    }

    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse> removeLike(@PathVariable Long postId, @Login UserDTO user) {
        postService.unlikePost(postId, user.getId());

        return ResponseEntity.ok(ApiResponse.builder().isSuccess(true).httpStatus(HttpStatus.OK)
            .message("게시물 좋아요를 취소했습니다.").build());
    }
}


