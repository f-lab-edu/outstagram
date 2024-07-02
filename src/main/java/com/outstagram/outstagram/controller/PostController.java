package com.outstagram.outstagram.controller;

import static com.outstagram.outstagram.common.constant.PageConst.PAGE_SIZE;

import com.outstagram.outstagram.common.annotation.Login;
import com.outstagram.outstagram.common.api.ApiResponse;
import com.outstagram.outstagram.controller.request.CreateCommentReq;
import com.outstagram.outstagram.controller.request.CreatePostReq;
import com.outstagram.outstagram.controller.request.EditCommentReq;
import com.outstagram.outstagram.controller.request.EditPostReq;
import com.outstagram.outstagram.controller.response.FeedRes;
import com.outstagram.outstagram.controller.response.MyPostsRes;
import com.outstagram.outstagram.dto.*;
import com.outstagram.outstagram.service.PostService;
import jakarta.validation.Valid;
import java.util.stream.Collectors;
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
    public ResponseEntity<MyPostsRes> getMyPosts(@RequestParam(required = false) Long lastId, @Login UserDTO user) {

        List<PostDetailsDTO> myPosts = postService.getMyPosts(user.getId(), lastId);

        MyPostsRes response = convertPostDetailsDtoToMyPostRes(myPosts);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailsDTO> getPost(@PathVariable Long postId, @Login UserDTO user) {
        PostDetailsDTO postDetailsDTO = postService.getPostDetails(postId, user.getId());

        return ResponseEntity.ok(postDetailsDTO);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PostDTO>> searchByKeyword(@RequestParam String keyword) {
        List<PostDTO> result = postService.findByKeyword(keyword);
        return ResponseEntity.ok(result);
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
    public ResponseEntity<MyPostsRes> getLikePosts(@RequestParam(required = false) Long lastId, @Login UserDTO user) {
        List<PostDetailsDTO> likePosts = postService.getLikePostsPlusOne(user.getId(), lastId);

        MyPostsRes response = convertPostDetailsDtoToMyPostRes(likePosts);

        return ResponseEntity.ok(response);
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
    public ResponseEntity<MyPostsRes> getBookmarkedPosts(@RequestParam(required = false) Long lastId, @Login UserDTO user) {
        List<PostDetailsDTO> bookmarkedPosts =  postService.getBookmarkedPostsPlusOne(user.getId(), lastId);

        MyPostsRes response = convertPostDetailsDtoToMyPostRes(bookmarkedPosts);

        return ResponseEntity.ok(response);
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
        postService.addComment(commentReq, postId, user.getId());

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
        postService.addComment(commentReq, postId, commentId, user.getId());

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
        List<PostDetailsDTO> feedList = postService.getFeed(lastId, user.getId());

        boolean hasNext = feedList.size() > PAGE_SIZE;

        List<FeedPostDTO> feedPostList = feedList.stream()
            .limit(PAGE_SIZE)
            .map(feed -> FeedPostDTO.builder()
                .postId(feed.getPostId())
                .postImgUrls(feed.getPostImgUrls())
                .contents(feed.getContents())
                .likeCount(feed.getLikes())
                .commentCount(feed.getComments().size())
                .likedByCurrentUser(feed.getLikedByCurrentUser())
                .bookmarkedByCurrentUser(feed.getBookmarkedByCurrentUser())
                .isCreatedByCurrentUser(feed.getIsCreatedByCurrentUser())

                .userId(feed.getUserId())
                .nickname(feed.getNickname())
                .userImgUrl(feed.getUserImgUrl())
                .build())
            .toList();

        return ResponseEntity.ok(
            FeedRes.builder()
                .feedPostDTOList(feedPostList)
                .hasNext(hasNext)
                .build()
        );
    }



    private static MyPostsRes convertPostDetailsDtoToMyPostRes(List<PostDetailsDTO> myPosts) {
        boolean hasNext = myPosts.size() > PAGE_SIZE;   // 가져온게 11개면 다음 페이지 존재함

        List<MyPostDTO> postList = myPosts.stream()
            .limit(PAGE_SIZE)
            .map(dto -> MyPostDTO.builder()
                .postId(dto.getPostId())
                .contents(dto.getContents())
                .likes(dto.getLikes())
                //이미지는 한 개만 가져오기
                .postThumbnailImage(dto.getPostImgUrls().values().stream().findFirst().orElse(null))
                .isLiked(dto.getLikedByCurrentUser())
                .isBookmarked(dto.getBookmarkedByCurrentUser())
                .commentCount(dto.getComments().size())
                .build())
            .collect(Collectors.toList());

        return MyPostsRes.builder()
            .postList(postList)
            .hasNext(hasNext)
            .build();
    }
}


