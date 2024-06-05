package com.outstagram.outstagram.controller;

import com.outstagram.outstagram.common.annotation.Login;
import com.outstagram.outstagram.common.api.ApiResponse;
import com.outstagram.outstagram.controller.request.CreateCommentReq;
import com.outstagram.outstagram.controller.request.CreatePostReq;
import com.outstagram.outstagram.controller.request.EditCommentReq;
import com.outstagram.outstagram.controller.request.EditPostReq;
import com.outstagram.outstagram.controller.response.FeedRes;
import com.outstagram.outstagram.controller.response.MyPostsRes;
import com.outstagram.outstagram.dto.PostDetailsDTO;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<PostDetailsDTO> getPost(@PathVariable Long postId, @Login UserDTO user) {
        PostDetailsDTO postDetailsDTO = postService.getPost(postId, user.getId());

        return ResponseEntity.ok(postDetailsDTO);
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
    public ResponseEntity<List<MyPostsRes>> getLikePosts(@RequestParam(required = false) Long lastId, @Login UserDTO user) {
        List<MyPostsRes> myLikePosts =  postService.getLikePosts(user.getId(), lastId);

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

    @GetMapping("/bookmarks")
    public ResponseEntity<List<MyPostsRes>> getBookmarkedPosts(@RequestParam(required = false) Long lastId, @Login UserDTO user) {
        List<MyPostsRes> myLikePosts =  postService.getBookmarkedPosts(user.getId(), lastId);

        return ResponseEntity.ok(myLikePosts);
    }

    @PostMapping("/{postId}/bookmark")
    public ResponseEntity<ApiResponse> addBookmark(@PathVariable Long postId, @Login UserDTO user) {
        postService.addBookmark(postId, user.getId());

        return ResponseEntity.ok(
            ApiResponse.builder()
                .isSuccess(true)
                .httpStatus(HttpStatus.OK)
                .message("게시물 북마크에 성공했습니다.")
                .build()
        );
    }

    @DeleteMapping("/{postId}/bookmark")
    public ResponseEntity<ApiResponse> removeBookmark(@PathVariable Long postId, @Login UserDTO user) {
        postService.deleteBookmark(postId, user.getId());

        return ResponseEntity.ok(ApiResponse.builder().isSuccess(true).httpStatus(HttpStatus.OK)
            .message("게시물 북마크를 취소했습니다.").build());
    }

    // 댓글 등록
    @PostMapping("/{postId}/comment")
    public ResponseEntity<ApiResponse> addComment(@PathVariable Long postId, @Login UserDTO user,
        @RequestBody CreateCommentReq commentReq) {
        postService.addComment(commentReq, postId, user);

        return ResponseEntity.ok(
            ApiResponse.builder()
                .isSuccess(true)
                .httpStatus(HttpStatus.OK)
                .message("댓글 등록에 성공했습니다.")
                .build()
        );
    }

    // 대댓글 등록
    @PostMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse> addReply(@PathVariable("postId") Long postId,
        @PathVariable("commentId") Long commentId,
        @RequestBody CreateCommentReq commentReq, @Login UserDTO user)
    {
        postService.addComment(commentReq, postId, commentId, user);

        return ResponseEntity.ok(
            ApiResponse.builder()
                .isSuccess(true)
                .httpStatus(HttpStatus.OK)
                .message("대댓글 등록에 성공했습니다.")
                .build()
        );
    }

    /**
     * (대)댓글 수정
     */
    @PatchMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse> editComment(@PathVariable("postId") Long postId,
        @PathVariable("commentId") Long commentId, @RequestBody EditCommentReq editCommentReq,
        @Login UserDTO user) {
        postService.editComment(editCommentReq, postId, commentId, user.getId());

        return ResponseEntity.ok(
            ApiResponse.builder()
                .isSuccess(true)
                .httpStatus(HttpStatus.OK)
                .message("댓글 수정에 성공했습니다.")
                .build()
        );
    }

    /**
     * (대)댓글 수정
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(@PathVariable("postId") Long postId,
        @PathVariable("commentId") Long commentId, @Login UserDTO user) {
        postService.deleteComment(postId, commentId, user.getId());

        return ResponseEntity.ok(
            ApiResponse.builder()
                .isSuccess(true)
                .httpStatus(HttpStatus.OK)
                .message("댓글 삭제에 성공했습니다.")
                .build()
        );
    }

    /**
     * 피드 불러오기
     */
    @GetMapping("/feed")
    public ResponseEntity<FeedRes> getFeed(@RequestParam(required = false) Long lastId, @Login UserDTO user) {
        FeedRes response = postService.getFeed(lastId, user.getId());

        return ResponseEntity.ok(response);
    }


}


